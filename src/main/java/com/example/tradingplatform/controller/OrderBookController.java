package com.example.tradingplatform.controller;

import com.example.tradingplatform.dto.OrderBookResponse;
import com.example.tradingplatform.service.OrderBookService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/instruments")
public class OrderBookController {

    private final OrderBookService orderBookService;

    public OrderBookController(OrderBookService orderBookService) {
        this.orderBookService = orderBookService;
    }

    @GetMapping("/{instrumentPublicId}/depth")
    public OrderBookResponse getDepth(
            @PathVariable UUID instrumentPublicId,
            @RequestParam(defaultValue = "10") int depth) {

        return orderBookService.getDepth(instrumentPublicId, depth);
    }
}