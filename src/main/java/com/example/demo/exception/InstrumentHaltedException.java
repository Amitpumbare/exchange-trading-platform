package com.example.demo.exception;

public class InstrumentHaltedException extends RuntimeException {
    public InstrumentHaltedException(Long instrumentId) {
        super("Instrument " + instrumentId + " is HALTED");
    }
}
