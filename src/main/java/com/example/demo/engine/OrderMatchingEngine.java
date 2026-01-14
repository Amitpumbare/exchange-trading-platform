package com.example.demo.engine;

import com.example.demo.model.Order;
import com.example.demo.model.OrderType;
import com.example.demo.model.OrderStatus;
import com.example.demo.model.Trade;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.TradeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import static java.lang.Math.min;

@Component
public class OrderMatchingEngine {

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;

    private final PriorityQueue<Order> buyBook;

    private final PriorityQueue<Order> sellBook;

    public OrderMatchingEngine(OrderRepository orderRepository,
                               TradeRepository tradeRepository) {

        this.orderRepository = orderRepository;
        this.tradeRepository = tradeRepository;

        this.buyBook = new PriorityQueue<>(
                (o1, o2) -> {
                    int priceCompare =
                            Double.compare(o2.getPrice(), o1.getPrice());

                    return priceCompare != 0
                            ? priceCompare
                            : o1.getCreatedAt().compareTo(o2.getCreatedAt());
                }
        );

        this.sellBook = new PriorityQueue<>(
                Comparator.comparingDouble(Order::getPrice).thenComparing(Order::getCreatedAt)
        );
    }

    @PostConstruct
    public void loadExistingOrders(){

        List<Order> orders = orderRepository.findByStatusIn(
                List.of(OrderStatus.OPEN,OrderStatus.PARTIALLY_FILLED)
        );

        for (Order o: orders){
            if(o.getType()==OrderType.BUY){
                buyBook.offer(o);
            }else{
                sellBook.offer(o);
            }
        }

        System.out.println(
                "Order book initialized -> buys: " + buyBook.size() +
                        ", sells: " + sellBook.size()
        );



    }

    public void removeOrder(Order order){
        if(order.getType() == OrderType.BUY){
            buyBook.remove(order);
        } else {
            sellBook.remove(order);
        }
    }

    public synchronized void process(Order order) {

        if (order.getType() == OrderType.BUY) {
            buyBook.offer(order);
        } else {
            sellBook.offer(order);
        }

        matchOrders();
    }

    private void matchOrders() {

        while (!buyBook.isEmpty()
                && !sellBook.isEmpty()
                && buyBook.peek().getPrice() >= sellBook.peek().getPrice()) {

            Order buy = buyBook.peek();
            Order sell = sellBook.peek();

            long matchedQty = Math.min(buy.getQuantity(), sell.getQuantity());

            updateOrderQuantities(buy, sell, matchedQty);

            recordTrade(buy, sell, matchedQty);

            orderRepository.save(buy);
            orderRepository.save(sell);
        }
    }

    private void updateOrderQuantities(Order buy, Order sell, long matchedQty) {

        long remainingBuy = buy.getQuantity() - matchedQty;
        long remainingSell = sell.getQuantity() - matchedQty;

        if (remainingBuy == 0) {
            buy.setQuantity(0);
            buy.setStatus(OrderStatus.FILLED);
            buy.setMessage("Order fully executed");
            buyBook.poll();
        } else {
            buy.setQuantity(remainingBuy);
            buy.setStatus(OrderStatus.PARTIALLY_FILLED);
            buy.setMessage("Partially filled. Waiting for remaining quantity");
        }

        if (remainingSell == 0) {
            sell.setQuantity(0);
            sell.setStatus(OrderStatus.FILLED);
            sell.setMessage("Order fully executed");
            sellBook.poll();
        } else {
            sell.setQuantity(remainingSell);
            sell.setStatus(OrderStatus.PARTIALLY_FILLED);
            sell.setMessage("Partially filled. Waiting for remaining quantity");
        }

    }

    private void recordTrade(Order buy, Order sell, long qty) {

        Trade trade = new Trade();
        trade.setBuyOrderId(buy.getId());
        trade.setSellOrderId(sell.getId());
        trade.setPrice(sell.getPrice());
        trade.setQuantity(qty);
        trade.setExecutedAt(Instant.now());

        tradeRepository.save(trade);
    }





}
