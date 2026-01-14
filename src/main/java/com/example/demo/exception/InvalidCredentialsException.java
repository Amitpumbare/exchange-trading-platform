package com.example.demo.exception;

public class InvalidCredentialsException extends TradingException{

    public InvalidCredentialsException(String msg){
        super(msg);
    }
}
