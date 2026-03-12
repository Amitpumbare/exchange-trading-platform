package com.example.tradingplatform.dto;

public class SignUpResponse {

    private String message;
    private String email;

    public SignUpResponse() {}

    public SignUpResponse(String message, String email) {
        this.message = message;
        this.email = email;
    }

    public String getMessage() { return message; }
    public String getEmail() { return email; }
}

