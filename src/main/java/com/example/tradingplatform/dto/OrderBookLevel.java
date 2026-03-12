package com.example.tradingplatform.dto;

public record OrderBookLevel(
        double price,
        long quantity
) {}