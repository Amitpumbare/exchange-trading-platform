package com.example.demo.exception;

import java.time.Instant;
import java.util.List;

public class ApiError {

    private int status;
    private String msg;
    private List<String> details;
    private Instant timestamp;

    public ApiError(int status, String msg, List<String> details, Instant timestamp) {
        this.status = status;
        this.msg = msg;
        this.details = details;
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
