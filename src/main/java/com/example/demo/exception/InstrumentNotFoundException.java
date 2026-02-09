package com.example.demo.exception;

public class InstrumentNotFoundException extends RuntimeException {
    public InstrumentNotFoundException(Long instrumentId) {
        super("Instrument not found with id: " + instrumentId);
    }
}
