package com.example.demo.controller;

import com.example.demo.dto.ModifyOrderRequest;
import com.example.demo.dto.OrderResponse;
import com.example.demo.dto.PlaceOrderRequest;
import com.example.demo.dto.TradeResponse;
import com.example.demo.model.Order;
import com.example.demo.model.Trade;
import com.example.demo.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping("/orders")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @PostMapping("/place-orders")
    public OrderResponse placeOrder(
            @Valid @RequestBody PlaceOrderRequest placeOrderRequest) {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        Long userId = (Long) auth.getPrincipal();

        Order order = orderService.createOrder(
                userId,
                placeOrderRequest.getType(),
                placeOrderRequest.getPrice(),
                placeOrderRequest.getQuantity(),
                placeOrderRequest.getInstrumentId()
        );

        return orderService.toOrderResponse(order);
    }

    @GetMapping("/get-orders")
    public List<OrderResponse> getOrders() {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        Long userId = (Long) auth.getPrincipal();

        boolean isAdmin = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return orderService.getAllOrderResponses();
        } else {
            return orderService.getOrderResponsesForUser(userId);
        }
    }

    @GetMapping("/get-trades")
    public List<TradeResponse> getTrades() {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        Long userId = (Long) auth.getPrincipal();

        boolean isAdmin = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if(isAdmin){
            return orderService.getAllTrades();
        }else{
            return orderService.getTradesForUser(userId);
        }
    }

    @GetMapping("/get-orderby-id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Order getOrderById(@PathVariable long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping("/get-tradeby-id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Trade getTradeById(@PathVariable long id) {
        return orderService.getTradebyId(id);
    }

    @PutMapping("/cancel-order/{id}")
    public OrderResponse cancelOrder(@PathVariable long id) {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        Long userId = (Long) auth.getPrincipal();

        Order cancelled = orderService.cancelOrderById(id, userId);

        return orderService.toOrderResponse(cancelled);
    }

    @PutMapping("/modify-order/{id}")
    public OrderResponse modifyOrder(
            @PathVariable long id,
            @Valid @RequestBody ModifyOrderRequest req
    ) {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        Long userId = (Long) auth.getPrincipal();

        Order modified = orderService.modifyOrderById(id, userId, req);

        return orderService.toOrderResponse(modified);
    }
}