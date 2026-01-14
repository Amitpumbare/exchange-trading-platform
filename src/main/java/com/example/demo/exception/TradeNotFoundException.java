package com.example.demo.exception;

public class TradeNotFoundException extends  TradingException{

    public TradeNotFoundException(Long id){
        super("Trade not Found with ID: " + id);
    }
}
