package com.example.demo.exception;

public class InvalidResetTokenException extends TradingException{
    public InvalidResetTokenException(String msg){
        super(msg);
    }
}
