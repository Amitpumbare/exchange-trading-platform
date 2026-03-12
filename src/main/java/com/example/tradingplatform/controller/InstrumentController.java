package com.example.tradingplatform.controller;

import com.example.tradingplatform.dto.InstrumentResponse;
import com.example.tradingplatform.service.InstrumentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/instruments")
public class InstrumentController {

    private final InstrumentService instrumentService;

    public InstrumentController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    /**
     * Read-only endpoint
     * USER + ADMIN
     * Used by frontend to show market list
     */
    @GetMapping("/get-instruments")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<InstrumentResponse> getAllInstruments() {
        return instrumentService.getAllInstruments();
    }
}
