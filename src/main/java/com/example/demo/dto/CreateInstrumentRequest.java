package com.example.demo.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateInstrumentRequest {

    @NotBlank(message = "Instrument symbol is required")
    @Size(min = 2, max = 20, message = "Instrument symbol must be 2–20 characters")
    @Pattern(
            regexp = "^[A-Z0-9-]+$",
            message = "Instrument symbol may contain only A–Z, 0–9, and '-'"
    )
    private String symbol;

    protected CreateInstrumentRequest() {}

    public String getSymbol() {
        return symbol;
    }
}
