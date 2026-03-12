package com.example.tradingplatform.exception;

public class OrderNotFoundException extends  TradingException{

    public  OrderNotFoundException(Long id){
        super("Order cannot be found with ID: " + id);
    }
}
