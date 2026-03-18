package com.example.tradingplatform.dto;

import java.io.Serializable;
import java.time.Instant;

public record TradeResponse(String instrumentSymbol, String side, double price, long quantity, Instant executedAt) implements Serializable {

}