package com.example.demo.controller;

import com.example.demo.dto.PlaceOrderRequest;
import com.example.demo.model.Order;
import com.example.demo.model.Trade;
import com.example.demo.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @PostMapping("/place-orders")
    public Order placeOrder(
            @Valid @RequestBody PlaceOrderRequest placeOrderRequest,
            HttpServletRequest request
    ) {
        Long userId = (Long) request.getAttribute("userId");

        return orderService.createOrder(
                userId,
                placeOrderRequest.getType(),
                placeOrderRequest.getPrice(),
                placeOrderRequest.getQuantity()
        );
    }

    @GetMapping("/get-orders")
    public List<Order> getOrders(HttpServletRequest request) {
        Object uid = request.getAttribute("userId");
        System.out.println("🔍 userId from request = " + uid);
        Long userId = (Long) request.getAttribute("userId");
        return orderService.getOrdersForUser(userId);
    }

    @GetMapping("/get-trades")
    public List<Trade> getTrades() {
        return orderService.getAllTrades();
    }

    @GetMapping("/get-orderby-id/{id}")
    public Order getOrderById(@PathVariable long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping("/get-tradeby-id/{id}")
    public Trade getTradeById(@PathVariable long id) {
        return orderService.getTradebyId(id);
    }

    // ===============================
    // CANCEL & MODIFY
    // Temporarily disabled during JWT rollout
    // Will be re-enabled with ownership checks
    // ===============================

    // @PutMapping("/cancel-order/{id}")
    // public Order cancelOrder(@PathVariable long id) {
    //     return orderService.cancelOrderById(id);
    // }

    // @PutMapping("/modify-order/{id}")
    // public Order modifyOrder(
    //         @PathVariable long id,
    //         @Valid @RequestBody ModifyOrderRequest req
    // ) {
    //     return orderService.modifyOrderById(id, req);
    // }
}
