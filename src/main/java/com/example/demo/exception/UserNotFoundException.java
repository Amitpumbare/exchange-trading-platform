package com.example.demo.exception;

public class UserNotFoundException extends TradingException {

    public UserNotFoundException(String message) {
        super(message);
    }
}

