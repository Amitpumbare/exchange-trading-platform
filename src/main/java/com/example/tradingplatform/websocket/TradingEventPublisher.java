package com.example.tradingplatform.websocket;

import com.example.tradingplatform.dto.OrderBookResponse;
import com.example.tradingplatform.dto.OrderResponse;
import com.example.tradingplatform.dto.TradeResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TradingEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public TradingEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // ================= TRADE EVENT =================

    public void sendTradeEvent(Long userId, TradeResponse trade) {

        String destination = "/topic/trades/" + userId;

        messagingTemplate.convertAndSend(destination, trade);

    }

    // ================= ORDER EVENT =================

    public void sendOrderEvent(Long userId, OrderResponse order) {

        String destination = "/topic/orders/" + userId;

        messagingTemplate.convertAndSend(destination, order);

    }

    // ================= DEPTH EVENT =================

    public void sendDepthEvent(UUID instrumentPublicId, OrderBookResponse depth) {

        String destination = "/topic/depth/" + instrumentPublicId;

        messagingTemplate.convertAndSend(destination, depth);
    }

}