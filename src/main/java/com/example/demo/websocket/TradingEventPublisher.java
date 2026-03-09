package com.example.demo.websocket;

import com.example.demo.dto.OrderResponse;
import com.example.demo.dto.TradeResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

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

}