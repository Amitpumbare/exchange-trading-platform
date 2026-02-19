package com.example.demo.controller;

import com.example.demo.dto.ModifyOrderRequest;
import com.example.demo.dto.PlaceOrderRequest;
import com.example.demo.model.Order;
import com.example.demo.model.Trade;
import com.example.demo.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
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
    public Order placeOrder(
            @Valid @RequestBody PlaceOrderRequest placeOrderRequest) {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();
        Long userId = (Long) auth.getPrincipal();

        return orderService.createOrder(
                userId,
                placeOrderRequest.getType(),
                placeOrderRequest.getPrice(),
                placeOrderRequest.getQuantity(),
                placeOrderRequest.getInstrumentId()
        );
    }

    @GetMapping("/get-orders")
    public List<Order> getOrders() {
         Authentication auth= SecurityContextHolder
                 .getContext()
                 .getAuthentication();

         Long userId = (Long) auth.getPrincipal();

         boolean isAdmin = auth.getAuthorities()
                 .stream()
                 .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

         if (isAdmin){
             return orderService.getAllOrders();
         }else{
             return orderService.getOrdersForUser(userId);
         }
    }

    @GetMapping("/get-trades")
    public List<Trade> getTrades() {

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
    public Order cancelOrder(@PathVariable long id) {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        Long userId = (Long) auth.getPrincipal();

        return orderService.cancelOrderById(id, userId);
    }

    @PutMapping("/modify-order/{id}")
    public Order modifyOrder(
            @PathVariable long id,
            @Valid @RequestBody ModifyOrderRequest req
    ) {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        Long userId = (Long) auth.getPrincipal();

        return orderService.modifyOrderById(id, userId, req);
    }
}
