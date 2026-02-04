package com.example.demo.service;

import com.example.demo.dto.ModifyOrderRequest;
import com.example.demo.engine.OrderMatchingEngine;
import com.example.demo.exception.*;
import com.example.demo.model.Order;
import com.example.demo.model.OrderStatus;
import com.example.demo.model.OrderType;
import com.example.demo.model.Trade;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.TradeRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMatchingEngine orderMatchingEngine;
    private final TradeRepository tradeRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderMatchingEngine orderMatchingEngine,
                        TradeRepository tradeRepository) {

        this.orderRepository = orderRepository;
        this.orderMatchingEngine = orderMatchingEngine;
        this.tradeRepository = tradeRepository;
    }

    // ================= CREATE ORDER =================

    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "allOrders", allEntries = true),
                    @CacheEvict(value = "ordersByUser", key = "#userId")
            }
    )
    public Order createOrder(Long userId, OrderType type, double price, long quantity) {

        Order order = new Order();
        order.setUserId(userId);
        order.setType(type);
        order.setPrice(price);
        order.setQuantity(quantity);
        order.setStatus(OrderStatus.OPEN);
        order.setCreatedAt(Instant.now());
        order.setMessage(getDefaultMessage(OrderStatus.OPEN));

        Order saved = orderRepository.save(order);

        orderMatchingEngine.process(saved);

        return orderRepository.findById(saved.getId())
                .orElseThrow(() -> new OrderNotFoundException(saved.getId()));
    }

    // ================= DEFAULT MESSAGE =================

    public String getDefaultMessage(OrderStatus status) {
        return switch (status) {
            case OPEN -> "Waiting for opposite orders at requested price";
            case PARTIALLY_FILLED -> "Partially filled. Waiting for remaining quantity";
            case FILLED -> "Order fully executed";
            case CANCELLED -> "Order cancelled by user";
            default -> "Unknown state";
        };
    }

    // ================= GET ORDERS =================

    @Cacheable(value = "ordersByUser", key = "#userId")
    public List<Order> getOrdersForUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    // ================= GET TRADES =================

    @Cacheable(value = "allTrades")
    public List<Trade> getAllTrades() {
        return tradeRepository.findAll();
    }

    @Cacheable(value = "orders", key = "#id")
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
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
            case FILLED ->
                    throw new OrderAlreadyFilledException(order.getId());
            case CANCELLED ->
                    throw new OrderAlreadyCancelledException(order.getId());
            case OPEN, PARTIALLY_FILLED -> {
                return cancelOpenOrPartial(order);
            }
            default -> {
                return order;
            }
        }
    }

    private Order cancelOpenOrPartial(Order order) {
        orderMatchingEngine.removeOrder(order);
        order.setStatus(OrderStatus.CANCELLED);
        order.setMessage(getDefaultMessage(OrderStatus.CANCELLED));
        return orderRepository.save(order);
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

        switch (order.getStatus()) {
            case FILLED ->
                    throw new OrderAlreadyFilledException(order.getId());
            case CANCELLED ->
                    throw new OrderAlreadyCancelledException(order.getId());
            case OPEN, PARTIALLY_FILLED -> {
                // allowed
            }
            default -> {
                return order;
            }
        }

        orderMatchingEngine.removeOrder(order);
        order.setStatus(OrderStatus.CANCELLED);
        order.setMessage(getDefaultMessage(OrderStatus.CANCELLED));
        orderRepository.save(order);

        return createOrder(
                currentUserId,
                req.getType(),
                req.getPrice(),
                req.getQuantity()
        );
    }
}
