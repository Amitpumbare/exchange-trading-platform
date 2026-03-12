package com.example.tradingplatform.service;

import com.example.tradingplatform.dto.ModifyOrderRequest;
import com.example.tradingplatform.dto.OrderResponse;
import com.example.tradingplatform.dto.TradeResponse;
import com.example.tradingplatform.dto.OrderBookResponse;
import com.example.tradingplatform.engine.InstrumentEngineRegistry;
import com.example.tradingplatform.engine.OrderMatchingEngine;
import com.example.tradingplatform.exception.*;
import com.example.tradingplatform.model.*;
import com.example.tradingplatform.repository.InstrumentRepository;
import com.example.tradingplatform.repository.OrderRepository;
import com.example.tradingplatform.repository.TradeRepository;
import com.example.tradingplatform.websocket.TradingEventPublisher;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final InstrumentEngineRegistry instrumentEngineRegistry;
    private final TradeRepository tradeRepository;
    private final InstrumentRepository instrumentRepository;
    private final TradingEventPublisher tradingEventPublisher;

    public OrderService(OrderRepository orderRepository,
                        InstrumentEngineRegistry instrumentEngineRegistry,
                        TradeRepository tradeRepository,
                        InstrumentRepository instrumentRepository,
                        TradingEventPublisher tradingEventPublisher) {

        this.orderRepository = orderRepository;
        this.instrumentEngineRegistry = instrumentEngineRegistry;
        this.tradeRepository = tradeRepository;
        this.instrumentRepository = instrumentRepository;
        this.tradingEventPublisher = tradingEventPublisher;
    }

    private void assertInstrumentActive(Long instrumentId) {
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new InvalidOrderRequestException("Instrument not Found"));

        if (instrument.getInstrumentStatus() == InstrumentStatus.HALTED) {
            throw new InstrumentHaltedException(instrumentId);
        }
    }

    // ================= HELPER =================

    public OrderResponse toOrderResponse(Order order) {

        Instrument instrument = instrumentRepository.findById(order.getInstrumentId())
                .orElseThrow(() -> new InvalidOrderRequestException("Instrument not found"));

        return new OrderResponse(
                order.getId(),
                instrument.getSymbol(),
                order.getType(),
                order.getPrice(),
                order.getQuantity(),
                order.getStatus(),
                order.getMessage()
        );
    }

    // ================= DEPTH PUBLISHER =================

    private void publishDepthUpdate(Long instrumentId) {

        Instrument instrument = instrumentRepository
                .findById(instrumentId)
                .orElseThrow(() -> new InvalidOrderRequestException("Instrument not found"));

        OrderMatchingEngine engine =
                instrumentEngineRegistry.getEngine(instrumentId);

        OrderBookResponse depth =
                engine.getOrderBookSnapshot(10);

        tradingEventPublisher.sendDepthEvent(
                instrument.getPublicId(),
                depth
        );
    }

    // ================= CREATE ORDER =================

    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "allOrders", allEntries = true),
                    @CacheEvict(value = "ordersByUser", key = "#userId")
            }
    )
    public Order createOrder(Long userId, OrderType type, double price, long quantity, UUID instrumentPublicId) {

        Instrument instrument = instrumentRepository
                .findByPublicId(instrumentPublicId)
                .orElseThrow(() -> new InvalidOrderRequestException("Instrument not found"));

        Long instrumentId = instrument.getId();

        assertInstrumentActive(instrumentId);

        OrderMatchingEngine engine = instrumentEngineRegistry.getEngine(instrumentId);

        Order order = new Order();
        order.setUserId(userId);
        order.setType(type);
        order.setPrice(price);
        order.setQuantity(quantity);
        order.setStatus(OrderStatus.OPEN);
        order.setCreatedAt(Instant.now());
        order.setMessage(getDefaultMessage(OrderStatus.OPEN));
        order.setInstrumentId(instrumentId);

        Order saved = orderRepository.save(order);

        engine.process(saved);

        // 🔴 DEPTH UPDATE
        publishDepthUpdate(instrumentId);

        List<Trade> trades =
                tradeRepository.findByBuyOrderIdOrSellOrderId(saved.getId(), saved.getId());

        for (Trade trade : trades) {

            TradeResponse buyerTrade = new TradeResponse(
                    instrument.getSymbol(),
                    "BUY",
                    trade.getPrice(),
                    trade.getQuantity(),
                    trade.getExecutedAt()
            );

            TradeResponse sellerTrade = new TradeResponse(
                    instrument.getSymbol(),
                    "SELL",
                    trade.getPrice(),
                    trade.getQuantity(),
                    trade.getExecutedAt()
            );

            tradingEventPublisher.sendTradeEvent(trade.getBuyerUserId(), buyerTrade);
            tradingEventPublisher.sendTradeEvent(trade.getSellerUserId(), sellerTrade);
        }

        Order updated = orderRepository.findById(saved.getId()).orElseThrow();

        tradingEventPublisher.sendOrderEvent(userId, toOrderResponse(updated));

        return updated;
    }

    public String getDefaultMessage(OrderStatus status) {
        return switch (status) {
            case OPEN -> "Waiting for opposite orders at requested price";
            case PARTIALLY_FILLED -> "Partially filled. Waiting for remaining quantity";
            case FILLED -> "Order fully executed";
            case CANCELLED -> "Order cancelled by user";
        };
    }

    @Cacheable(value = "ordersByUser", key = "#userId")
    public List<OrderResponse> getOrderResponsesForUser(Long userId) {
        return orderRepository.findOrderResponsesForUser(userId);
    }

    @Cacheable(value = "allOrders")
    public List<OrderResponse> getAllOrderResponses() {
        return orderRepository.findAllOrderResponses();
    }

    @Cacheable(value = "orders", key = "#id")
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    public List<TradeResponse> getTradesForUser(Long userId) {
        return tradeRepository.findTradeResponsesForUser(userId);
    }

    @Cacheable(value = "allTrades")
    public List<TradeResponse> getAllTrades() {
        return tradeRepository.findAllTradeResponses();
    }

    @Cacheable(value = "trades", key = "#id")
    public Trade getTradebyId(Long id) {
        return tradeRepository.findById(id)
                .orElseThrow(() -> new TradeNotFoundException(id));
    }

    // ================= CANCEL ORDER =================

    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "orders", key = "#id"),
                    @CacheEvict(value = "ordersByUser", key = "#currentUserId"),
                    @CacheEvict(value = "allOrders", allEntries = true)
            }
    )
    public Order cancelOrderById(Long id, Long currentUserId) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (!order.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not own this order");
        }

        switch (order.getStatus()) {
            case FILLED -> throw new OrderAlreadyFilledException(order.getId());
            case CANCELLED -> throw new OrderAlreadyCancelledException(order.getId());
            case OPEN, PARTIALLY_FILLED -> {
                return cancelOpenOrPartial(order);
            }
            default -> {
                return order;
            }
        }
    }

    private Order cancelOpenOrPartial(Order order) {

        OrderMatchingEngine engine = instrumentEngineRegistry.getEngine(order.getInstrumentId());

        engine.removeOrder(order);

        // 🔴 DEPTH UPDATE
        publishDepthUpdate(order.getInstrumentId());

        order.setStatus(OrderStatus.CANCELLED);
        order.setMessage(getDefaultMessage(OrderStatus.CANCELLED));

        Order cancelled = orderRepository.save(order);

        tradingEventPublisher.sendOrderEvent(order.getUserId(), toOrderResponse(cancelled));

        return cancelled;
    }

    // ================= MODIFY ORDER =================

    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "orders", key = "#id"),
                    @CacheEvict(value = "ordersByUser", key = "#currentUserId"),
                    @CacheEvict(value = "allOrders", allEntries = true)
            }
    )
    public Order modifyOrderById(Long id, Long currentUserId, ModifyOrderRequest req) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (!order.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not own this order");
        }

        Instrument instrument = instrumentRepository
                .findByPublicId(req.getInstrumentId())
                .orElseThrow(() -> new InvalidOrderRequestException("Instrument not found"));

        Long instrumentId = instrument.getId();

        if (!instrumentId.equals(order.getInstrumentId())) {
            throw new InstrumentChangeNotAllowedException(
                    "Instrument cannot be changed from "
                            + order.getInstrumentId()
                            + " to "
                            + instrumentId
            );
        }

        assertInstrumentActive(instrumentId);

        OrderMatchingEngine engine = instrumentEngineRegistry.getEngine(order.getInstrumentId());

        engine.removeOrder(order);

        // 🔴 DEPTH UPDATE
        publishDepthUpdate(order.getInstrumentId());

        order.setStatus(OrderStatus.CANCELLED);
        order.setMessage(getDefaultMessage(OrderStatus.CANCELLED));

        Order cancelled = orderRepository.save(order);

        tradingEventPublisher.sendOrderEvent(order.getUserId(), toOrderResponse(cancelled));

        return createOrder(
                currentUserId,
                req.getType(),
                req.getPrice(),
                req.getQuantity(),
                req.getInstrumentId()
        );
    }
}