package com.example.tradingplatform.dto;

import java.util.UUID;

public record InstrumentResponse(
        UUID instrumentId,
        String symbol,
        String instrumentStatus
) {}
