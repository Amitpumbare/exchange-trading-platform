package com.example.tradingplatform.service;

import com.example.tradingplatform.dto.OrderBookResponse;
import com.example.tradingplatform.engine.InstrumentEngineRegistry;
import com.example.tradingplatform.engine.OrderMatchingEngine;
import com.example.tradingplatform.model.Instrument;
import com.example.tradingplatform.repository.InstrumentRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderBookService {

    private final InstrumentEngineRegistry registry;
    private final InstrumentRepository instrumentRepository;

    public OrderBookService(
            InstrumentEngineRegistry registry,
            InstrumentRepository instrumentRepository
    ) {
        this.registry = registry;
        this.instrumentRepository = instrumentRepository;
    }

    public OrderBookResponse getDepth(UUID instrumentPublicId, int depth) {

        Instrument instrument = instrumentRepository
                .findByPublicId(instrumentPublicId)
                .orElseThrow(() -> new RuntimeException("Instrument not found"));

        Long instrumentId = instrument.getId();

        OrderMatchingEngine engine =
                registry.getEngine(instrumentId);

        return engine.getOrderBookSnapshot(depth);
    }
}