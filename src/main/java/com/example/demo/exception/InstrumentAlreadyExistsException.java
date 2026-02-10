package com.example.demo.exception;

public class InstrumentAlreadyExistsException extends  TradingException{

    public InstrumentAlreadyExistsException(String symbol){
        super("Instrument" + symbol + "already exists");
    }
}
