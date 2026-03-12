package com.example.tradingplatform.repository;

import com.example.tradingplatform.model.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface InstrumentRepository extends JpaRepository<Instrument, Long> {
    boolean existsBySymbol(String symbol);
    Optional<Instrument> findByPublicId(UUID publicId);
}
