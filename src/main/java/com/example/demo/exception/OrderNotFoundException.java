package com.example.demo.exception;

public class OrderNotFoundException extends  TradingException{

    public  OrderNotFoundException(Long id){
        super("Order cannot be found with ID: " + id);
    }
}
