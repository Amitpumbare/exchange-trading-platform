package com.example.tradingplatform.dto;

import java.util.List;

public record OrderBookResponse(
        List<OrderBookLevel> bids,
        List<OrderBookLevel> asks
) {}