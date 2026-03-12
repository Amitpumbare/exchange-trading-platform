package com.example.tradingplatform.exception;

public class InstrumentNotFoundException extends RuntimeException {
    public InstrumentNotFoundException(Long instrumentId) {
        super("Instrument not found with id: " + instrumentId);
    }
}
