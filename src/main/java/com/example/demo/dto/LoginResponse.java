package com.example.demo.dto;

public class LoginResponse {

    private String message;
    private Long id;
    private String fullName;
    private String email;

    public LoginResponse(String message, Long id, String fullName, String email) {
        this.message = message;
        this.id = id;
        this.fullName = fullName;
        this.email = email;
    }

    public LoginResponse(){}

    public String getMessage() { return message; }
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
}

