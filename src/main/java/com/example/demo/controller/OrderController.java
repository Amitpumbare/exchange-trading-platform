package com.example.demo.controller;

import com.example.demo.dto.ModifyOrderRequest;
import com.example.demo.dto.PlaceOrderRequest;
import com.example.demo.model.Order;
import com.example.demo.model.OrderType;
import com.example.demo.model.Trade;
import com.example.demo.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService){
        this.orderService=orderService;
    }

    @PostMapping("/place-orders")
    public Order PlaceOrder( @Valid @RequestBody PlaceOrderRequest placeOrderRequest){
        return orderService.createOrder(placeOrderRequest.getType(),placeOrderRequest.getPrice(),placeOrderRequest.getQuantity());
    }

    @GetMapping("/get-orders")
    public List<Order> getorder(){
        return orderService.getALLOrders();
    }

    @GetMapping("/get-trades")
    public List<Trade> gettrade(){
        return orderService.getAllTrades();
    }

    @GetMapping("/get-orderby-id/{id}")
    public Order getorderbyId(@PathVariable long id){
        return orderService.getOrderById(id);
    }

    @GetMapping("/get-tradeby-id/{id}")
    public Trade gettradebyId(@PathVariable long id){
        return orderService.getTradebyId(id);
    }

    @PutMapping("/cancel-order/{id}")
    public Order cancelOrder(@PathVariable long id){
        return orderService.cancelOrderById(id);
    }

    @PutMapping("/modify-order/{id}")
    public Order modifyOrder(@PathVariable long id, @Valid @RequestBody ModifyOrderRequest modifyOrderRequest) { return  orderService.modifyOrderById(id,modifyOrderRequest); }



}
