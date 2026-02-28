package com.example.demo.dto;

import com.example.demo.model.OrderStatus;
import com.example.demo.model.OrderType;

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