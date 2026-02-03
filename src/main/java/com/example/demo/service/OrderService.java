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

    // CREATE ORDER
    @Transactional
    @CacheEvict(value = "allOrders", allEntries = true)
    public Order createOrder(Long userId, OrderType type, double price, long quantity) {

        // 1️⃣ Create order
        Order order = new Order();
        order.setUserId(userId);
        order.setType(type);
        order.setPrice(price);
        order.setQuantity(quantity);
        order.setStatus(OrderStatus.OPEN);
        order.setCreatedAt(Instant.now());
        order.setMessage(getDefaultMessage(OrderStatus.OPEN));

        // 2️⃣ Save so it gets an ID
        Order saved = orderRepository.save(order);

        // 3️⃣ Let engine process it (engine updates status + message)
        orderMatchingEngine.process(saved);

        // 4️⃣ Reload from DB to avoid stale Hibernate object
        return orderRepository.findById(saved.getId())
                .orElseThrow(() -> new OrderNotFoundException(saved.getId()));
    }






    public String getDefaultMessage(OrderStatus status) {
        return switch (status) {
            case OPEN -> "Waiting for opposite orders at requested price";
            case PARTIALLY_FILLED -> "Partially filled. Waiting for remaining quantity";
            case FILLED -> "Order fully executed";
            case CANCELLED -> "Order cancelled by user";
            default -> "Unknown state";
        };
    }


    // GET ALL ORDERS
    @Cacheable(value = "ordersByUser", key = "#userId")
    public List<Order> getOrdersForUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }


    // GET ALL TRADES
    @Cacheable(value = "allTrades")
    public List<Trade> getAllTrades() {
        return tradeRepository.findAll();
    }

    // GET ORDER BY ID
    @Cacheable(value = "orders", key = "#id")
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    // GET TRADE BY ID
    @Cacheable(value = "trades", key = "#id")
    public Trade getTradebyId(Long id) {
        return tradeRepository.findById(id)
                .orElseThrow(() -> new TradeNotFoundException(id));
    }


    @Caching(
            evict = {
                    @CacheEvict(value = "orders", key = "#id"),
                    @CacheEvict(value = "allOrders", allEntries = true)
            }
    )
    @Transactional
    public Order cancelOrderById(Long id, Long currentUserId) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        // 🔐 OWNERSHIP CHECK
        if (!order.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException(
                    "You do not own this order"
            );
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

    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "orders", key = "#id"),
                    @CacheEvict(value = "allOrders", allEntries = true)
            }
    )
    public Order modifyOrderById(Long id, Long currentUserId, ModifyOrderRequest req) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        // 🔐 OWNERSHIP CHECK
        if (!order.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not own this order");
        }

        // ❌ Cannot modify terminal states
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

        // 1️⃣ Cancel existing order (engine + status)
        orderMatchingEngine.removeOrder(order);
        order.setStatus(OrderStatus.CANCELLED);
        order.setMessage(getDefaultMessage(OrderStatus.CANCELLED));
        orderRepository.save(order);

        // 2️⃣ Create new order with SAME userId
        return createOrder(
                currentUserId,
                req.getType(),
                req.getPrice(),
                req.getQuantity()
        );
    }


}


