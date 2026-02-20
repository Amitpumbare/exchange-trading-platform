package com.example.demo.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(
        name = "instruments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_instrument_symbol", columnNames = "symbol")
        },
        indexes = {
                @Index(name = "idx_symbol", columnList = "symbol")
        }
)
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstrumentStatus instrumentStatus;

    @Column(nullable = false, updatable = false, unique = true)
    private UUID publicId;

    protected Instrument() {
        // JPA only
    }

    public Instrument(String symbol, InstrumentStatus instrumentStatus) {
        this.symbol = symbol;
        this.instrumentStatus = instrumentStatus;
    }

    public Long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public InstrumentStatus getInstrumentStatus() {
        return instrumentStatus;
    }

    public void setInstrumentStatus(InstrumentStatus instrumentStatus) { this.instrumentStatus = instrumentStatus; }

    @PrePersist
    public  void generatePublicId(){
        if( publicId == null){
            publicId = UUID.randomUUID();
        }
    }

    public UUID getPublicId() {
        return publicId;
    }
}
