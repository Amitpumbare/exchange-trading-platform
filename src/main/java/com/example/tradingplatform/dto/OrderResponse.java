package com.example.tradingplatform.dto;

import com.example.tradingplatform.model.OrderStatus;
import com.example.tradingplatform.model.OrderType;

import java.io.Serializable;

public record OrderResponse(

        Long id,
        String instrumentSymbol,
        OrderType type,
        double price,
        long quantity,
        OrderStatus status,
        String message

) implements Serializable {}