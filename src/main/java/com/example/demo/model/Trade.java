package com.example.demo.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "trades")
public class Trade implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== NEW (USERS) =====
    @Column(nullable = false)
    private Long buyerUserId;

    @Column(nullable = false)
    private Long sellerUserId;

    // ===== EXISTING (ORDERS) =====
    @Column(nullable = false)
    private Long buyOrderId;

    @Column(nullable = false)
    private Long sellOrderId;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private long quantity;

    @Column(nullable = false, updatable = false)
    private Instant executedAt;

    // -------- Constructors --------

    public Trade() {
    }

    public Trade(Long id,
                 Long buyerUserId,
                 Long sellerUserId,
                 Long buyOrderId,
                 Long sellOrderId,
                 double price,
                 long quantity,
                 Instant executedAt) {
        this.id = id;
        this.buyerUserId = buyerUserId;
        this.sellerUserId = sellerUserId;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.price = price;
        this.quantity = quantity;
        this.executedAt = executedAt;
    }

    // -------- Getters & Setters --------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBuyerUserId() {
        return buyerUserId;
    }

    public void setBuyerUserId(Long buyerUserId) {
        this.buyerUserId = buyerUserId;
    }

    public Long getSellerUserId() {
        return sellerUserId;
    }

    public void setSellerUserId(Long sellerUserId) {
        this.sellerUserId = sellerUserId;
    }

    public Long getBuyOrderId() {
        return buyOrderId;
    }

    public void setBuyOrderId(Long buyOrderId) {
        this.buyOrderId = buyOrderId;
    }

    public Long getSellOrderId() {
        return sellOrderId;
    }

    public void setSellOrderId(Long sellOrderId) {
        this.sellOrderId = sellOrderId;
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

    public Instant getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(Instant executedAt) {
        this.executedAt = executedAt;
    }
}
