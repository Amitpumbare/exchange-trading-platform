package com.example.demo.controller;

import com.example.demo.exception.InstrumentNotFoundException;
import com.example.demo.model.Instrument;
import com.example.demo.model.InstrumentStatus;
import com.example.demo.model.Order;
import com.example.demo.model.Trade;
import com.example.demo.repository.InstrumentRepository;
import com.example.demo.service.OrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final InstrumentRepository instrumentRepository;
    private final OrderService orderService;

    public AdminController(InstrumentRepository instrumentRepository,
                           OrderService orderService) {
        this.instrumentRepository = instrumentRepository;
        this.orderService = orderService;
    }

    // ================= INSTRUMENT CONTROL =================

    @PostMapping("/instruments/{instrumentId}/halt")
    protected Instrument haltInstrument(@PathVariable Long instrumentId) {
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new InstrumentNotFoundException(instrumentId));

        instrument.setInstrumentStatus(InstrumentStatus.HALTED);
        return instrumentRepository.save(instrument);
    }

    @PostMapping("/instruments/{instrumentId}/resume")
    protected Instrument resumeInstrument(@PathVariable Long instrumentId) {
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new InstrumentNotFoundException(instrumentId));

        instrument.setInstrumentStatus(InstrumentStatus.ACTIVE);
        return instrumentRepository.save(instrument);
    }

    // ================= VISIBILITY =================

    @GetMapping("/instruments")
    public List<Instrument> getAllInstruments() {
        return instrumentRepository.findAll();
    }

    @GetMapping("/orders")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/trades")
    public List<Trade> getAllTrades() {
        return orderService.getAllTrades();
    }
}

