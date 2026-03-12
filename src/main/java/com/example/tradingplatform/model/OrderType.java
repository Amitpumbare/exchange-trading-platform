package com.example.tradingplatform.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum OrderType {
    BUY, SELL;

    @JsonCreator
    public static OrderType from(String value) {
        return OrderType.valueOf(value.toUpperCase());
    }
}

