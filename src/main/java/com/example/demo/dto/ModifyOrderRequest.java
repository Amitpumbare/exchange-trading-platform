package com.example.demo.dto;

import com.example.demo.model.OrderType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ModifyOrderRequest {

    @NotNull(message = "Ordes should have an type")
    private OrderType type;

    @Positive(message = "Price should be greater than 0")
    private double price;

    @Positive(message = "Quantity should be greater than 0")
    private long quantity;


    // -------- Constructors --------
    public ModifyOrderRequest() {
    }

    public ModifyOrderRequest(OrderType type, double price, long quantity) {
        this.type = type;
        this.price = price;
        this.quantity = quantity;
    }

    // -------- Getters & Setters --------
    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }
}
