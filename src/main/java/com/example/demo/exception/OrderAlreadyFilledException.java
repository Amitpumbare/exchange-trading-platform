package com.example.demo.exception;

public class OrderAlreadyFilledException extends  TradingException{

    public  OrderAlreadyFilledException(Long id){
        super("Order is already filled with ID: " + id);
    }
}
