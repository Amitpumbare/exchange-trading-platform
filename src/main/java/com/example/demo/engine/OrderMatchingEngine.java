package com.example.demo.engine;

import com.example.demo.model.Order;
import com.example.demo.model.OrderType;
import com.example.demo.model.OrderStatus;
import com.example.demo.model.Trade;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.TradeRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;



public class OrderMatchingEngine {

    private final Long instrumentId;
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;


    private final PriorityQueue<Order> buyBook;

    private final PriorityQueue<Order> sellBook;

    public OrderMatchingEngine(Long instrumentId, OrderRepository orderRepository,
                               TradeRepository tradeRepository) {

        this.instrumentId=instrumentId;
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

        loadExistingOrders();
    }

    private void loadExistingOrders(){

        // 🔴 MUST clear to avoid duplicates on restart
        buyBook.clear();
        sellBook.clear();

        List<Order> orders = orderRepository.findByInstrumentIdAndStatusIn(instrumentId, List.of(OrderStatus.PARTIALLY_FILLED, OrderStatus.OPEN));
        for (Order o : orders){
            if(o.getType() == OrderType.BUY){
                buyBook.offer(o);
            } else {
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

        // 🚫 Deduplicate first (CRITICAL)
        buyBook.remove(order);
        sellBook.remove(order);

        if (order.getStatus() == OrderStatus.OPEN
                || order.getStatus() == OrderStatus.PARTIALLY_FILLED) {

            if (order.getType() == OrderType.BUY) {
                buyBook.offer(order);
            } else {
                sellBook.offer(order);
            }
        }

        matchOrders();
    }


    private void matchOrders() {

        while (!buyBook.isEmpty()
                && !sellBook.isEmpty()
                && buyBook.peek().getPrice() >= sellBook.peek().getPrice()) {

            // 🔴 REMOVE from heap FIRST
            Order buy = buyBook.poll();
            Order sell = sellBook.poll();

            long matchedQty = Math.min(buy.getQuantity(), sell.getQuantity());

            // ---- BUY ----
            long remainingBuy = buy.getQuantity() - matchedQty;
            if (remainingBuy == 0) {
                buy.setQuantity(0);
                buy.setStatus(OrderStatus.FILLED);
                buy.setMessage("Order fully executed");
            } else {
                buy.setQuantity(remainingBuy);
                buy.setStatus(OrderStatus.PARTIALLY_FILLED);
                buy.setMessage("Partially filled. Waiting for remaining quantity");
            }

            // ---- SELL ----
            long remainingSell = sell.getQuantity() - matchedQty;
            if (remainingSell == 0) {
                sell.setQuantity(0);
                sell.setStatus(OrderStatus.FILLED);
                sell.setMessage("Order fully executed");
            } else {
                sell.setQuantity(remainingSell);
                sell.setStatus(OrderStatus.PARTIALLY_FILLED);
                sell.setMessage("Partially filled. Waiting for remaining quantity");
            }

            // Trade
            recordTrade(buy, sell, matchedQty);

            // Persist
            orderRepository.save(buy);
            orderRepository.save(sell);

            // 🔁 REINSERT only if still active
            if (buy.getStatus() == OrderStatus.OPEN
                    || buy.getStatus() == OrderStatus.PARTIALLY_FILLED) {
                buyBook.offer(buy);
            }

            if (sell.getStatus() == OrderStatus.OPEN
                    || sell.getStatus() == OrderStatus.PARTIALLY_FILLED) {
                sellBook.offer(sell);
            }
        }
    }


    private void recordTrade(Order buy, Order sell, long qty) {

        Trade trade = new Trade();
        trade.setBuyerUserId(buy.getUserId());
        trade.setSellerUserId(sell.getUserId());
        trade.setBuyOrderId(buy.getId());
        trade.setSellOrderId(sell.getId());
        trade.setPrice(sell.getPrice());
        trade.setQuantity(qty);
        trade.setExecutedAt(Instant.now());

        tradeRepository.save(trade);
    }





}
