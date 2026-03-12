package com.example.tradingplatform.repository;

import com.example.tradingplatform.dto.TradeResponse;
import com.example.tradingplatform.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    // USER trades (user perspective BUY / SELL)
    @Query("""
        SELECT new com.example.demo.dto.TradeResponse(
            i.symbol,
            CASE
                WHEN t.buyerUserId = :userId THEN 'BUY'
                ELSE 'SELL'
            END,
            t.price,
            t.quantity,
            t.executedAt
        )
        FROM Trade t
        JOIN Order o ON o.id = t.buyOrderId
        JOIN Instrument i ON i.id = o.instrumentId
        WHERE t.buyerUserId = :userId
           OR t.sellerUserId = :userId
        ORDER BY t.executedAt DESC
    """)
    List<TradeResponse> findTradeResponsesForUser(Long userId);


    // ADMIN trades (no perspective)
    @Query("""
        SELECT new com.example.demo.dto.TradeResponse(
            i.symbol,
            'EXECUTED',
            t.price,
            t.quantity,
            t.executedAt
        )
        FROM Trade t
        JOIN Order o ON o.id = t.buyOrderId
        JOIN Instrument i ON i.id = o.instrumentId
        ORDER BY t.executedAt DESC
    """)
    List<TradeResponse> findAllTradeResponses();

    List<Trade> findByBuyOrderIdOrSellOrderId(Long buyOrderId, Long sellOrderId);

}