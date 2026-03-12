package com.example.tradingplatform.exception;

public class InstrumentChangeNotAllowedException extends TradingException{

    public InstrumentChangeNotAllowedException(String msg){
        super(msg);
    }
}
