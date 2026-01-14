package com.example.demo.exception;

public class InvalidOrderRequestException extends  TradingException{

    public InvalidOrderRequestException(String msg) {
        super(msg);
    }

}
