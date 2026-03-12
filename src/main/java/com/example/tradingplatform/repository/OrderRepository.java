package com.example.tradingplatform.repository;

import com.example.tradingplatform.dto.OrderResponse;
import com.example.tradingplatform.model.Order;
import com.example.tradingplatform.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByInstrumentIdAndStatusIn(Long instrumentId, List<OrderStatus> statuses);

    List<Order> findByMessageIsNull();

    List<Order> findByUserId(Long userId);


    // ✅ USER ORDER RESPONSES (JOIN projection)
    @Query("""
        SELECT new com.example.tradingplatform.dto.OrderResponse(
            o.id,
            i.symbol,
            o.type,
            o.price,
            o.quantity,
            o.status,
            o.message
        )
        FROM Order o
        JOIN Instrument i ON i.id = o.instrumentId
        WHERE o.userId = :userId
        ORDER BY o.createdAt DESC
    """)
    List<OrderResponse> findOrderResponsesForUser(Long userId);


    // ✅ ADMIN ORDER RESPONSES
    @Query("""
        SELECT new com.example.tradingplatform.dto.OrderResponse(
            o.id,
            i.symbol,
            o.type,
            o.price,
            o.quantity,
            o.status,
            o.message
        )
        FROM Order o
        JOIN Instrument i ON i.id = o.instrumentId
        ORDER BY o.createdAt DESC
    """)
    List<OrderResponse> findAllOrderResponses();

}