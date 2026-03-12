package com.example.tradingplatform.exception;

public class InstrumentAlreadyExistsException extends  TradingException{

    public InstrumentAlreadyExistsException(String symbol){
        super("Instrument" + symbol + "already exists");
    }
}
