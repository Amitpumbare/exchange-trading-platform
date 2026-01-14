package com.example.demo.service;

import com.example.demo.model.Order;
import com.example.demo.model.OrderStatus;
import com.example.demo.repository.OrderRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMessageBackfill {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public OrderMessageBackfill(OrderRepository orderRepository,
                                OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @PostConstruct
    public void backfillMessages() {

        List<Order> orders = orderRepository.findByMessageIsNull();

        for (Order order : orders) {
            String msg = orderService.getDefaultMessage(order.getStatus());
            order.setMessage(msg);
        }

        orderRepository.saveAll(orders);

        System.out.println("Backfilled " + orders.size() + " orders with messages");
    }
}
