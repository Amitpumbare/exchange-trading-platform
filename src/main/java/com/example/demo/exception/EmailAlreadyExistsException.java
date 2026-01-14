package com.example.demo.exception;

import jakarta.servlet.http.PushBuilder;

public class EmailAlreadyExistsException extends TradingException{

    public EmailAlreadyExistsException(String msg){
        super(msg);
    }

}
