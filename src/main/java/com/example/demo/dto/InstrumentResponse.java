package com.example.demo.dto;

import java.util.UUID;

public record InstrumentResponse(
        UUID instrumentId,
        String symbol,
        String instrumentStatus
) {}
