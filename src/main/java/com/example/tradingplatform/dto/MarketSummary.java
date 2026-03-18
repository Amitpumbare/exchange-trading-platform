package com.example.tradingplatform.dto;

public record MarketSummary(
        Double bestBid,
        Double bestAsk,
        Double spread
) {}
