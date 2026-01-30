package com.example.demo.dto;

public class LoginResponse {

    private String token;
    private Long id;
    private String fullName;
    private String email;

    public LoginResponse(String token, Long id, String fullName, String email) {
        this.token = token;
        this.id = id;
        this.fullName = fullName;
        this.email = email;
    }

    public LoginResponse() {}

    public String getToken() { return token; }
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
}
