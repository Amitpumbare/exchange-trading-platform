package com.example.tradingplatform.service;

import com.example.tradingplatform.dto.InstrumentResponse;
import com.example.tradingplatform.exception.InstrumentAlreadyExistsException;
import com.example.tradingplatform.model.Instrument;
import com.example.tradingplatform.model.InstrumentStatus;
import com.example.tradingplatform.repository.InstrumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;

    public InstrumentService(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    public Instrument createInstrument(String symbol) {
        String normalizedSymbol = symbol.toUpperCase();

        if (instrumentRepository.existsBySymbol(normalizedSymbol)) {
            throw new InstrumentAlreadyExistsException(normalizedSymbol);
        }

        Instrument instrument = new Instrument(
                normalizedSymbol,
                InstrumentStatus.ACTIVE
        );

        return instrumentRepository.save(instrument);
    }

    public List<InstrumentResponse> getAllInstruments() {
        return instrumentRepository.findAll()
                .stream()
                .map(instrument -> new InstrumentResponse(
                        instrument.getPublicId(),
                        instrument.getSymbol(),
                        instrument.getInstrumentStatus().name()
                ))
                .toList();
    }
}

