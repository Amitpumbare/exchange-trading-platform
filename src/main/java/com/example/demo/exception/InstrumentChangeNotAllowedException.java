package com.example.demo.exception;

public class InstrumentChangeNotAllowedException extends TradingException{

    public InstrumentChangeNotAllowedException(String msg){
        super(msg);
    }
}
