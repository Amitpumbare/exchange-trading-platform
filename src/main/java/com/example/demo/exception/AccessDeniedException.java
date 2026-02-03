package com.example.demo.exception;

public class AccessDeniedException extends TradingException {

    public AccessDeniedException(String msg){
        super(msg);
    }
}
