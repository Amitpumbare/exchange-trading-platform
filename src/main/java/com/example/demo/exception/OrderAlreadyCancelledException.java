package com.example.demo.exception;

public class OrderAlreadyCancelledException extends  TradingException{

    public OrderAlreadyCancelledException(Long id){
        super("Order is already Cancelled with ID: " + id);
    }

}
