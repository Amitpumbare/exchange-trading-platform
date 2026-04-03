package com.example.tradingplatform.engine;

import com.example.tradingplatform.dto.OrderBookLevel;
import com.example.tradingplatform.dto.OrderBookResponse;
import com.example.tradingplatform.model.Order;
import com.example.tradingplatform.model.OrderType;
import com.example.tradingplatform.model.OrderStatus;
import com.example.tradingplatform.model.Trade;
import com.example.tradingplatform.repository.OrderRepository;
import com.example.tradingplatform.repository.TradeRepository;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class OrderMatchingEngine {

    private final Long instrumentId;
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;

    private final PriorityQueue<Order> buyBook;
    private final PriorityQueue<Order> sellBook;

    public OrderMatchingEngine(Long instrumentId,
                               OrderRepository orderRepository,
                               TradeRepository tradeRepository) {

        this.instrumentId = instrumentId;
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
                Comparator.comparingDouble(Order::getPrice)
                        .thenComparing(Order::getCreatedAt)
        );

        loadExistingOrders();
    }

    private void loadExistingOrders(){

        buyBook.clear();
        sellBook.clear();

        List<Order> orders =
                orderRepository.findByInstrumentIdAndStatusIn(
                        instrumentId,
                        List.of(OrderStatus.PARTIALLY_FILLED, OrderStatus.OPEN)
                );

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

            Order buy = buyBook.poll();
            Order sell = sellBook.poll();

            if (buy.getUserId().equals(sell.getUserId())) {
                buyBook.offer(buy);
                sellBook.offer(sell);
                break;
            }

            // ✅ USE REMAINING QUANTITY
            long buyRemaining = buy.getRemainingQuantity();
            long sellRemaining = sell.getRemainingQuantity();

            long matchedQty = Math.min(buyRemaining, sellRemaining);

            // ✅ UPDATE EXECUTED QUANTITY (NOT quantity)
            buy.setExecutedQuantity(
                    buy.getExecutedQuantity() + matchedQty
            );

            sell.setExecutedQuantity(
                    sell.getExecutedQuantity() + matchedQty
            );

            // ✅ UPDATE STATUS BASED ON EXECUTION
            if (buy.getExecutedQuantity() == buy.getQuantity()) {
                buy.setStatus(OrderStatus.FILLED);
                buy.setMessage("Order fully executed");
            } else {
                buy.setStatus(OrderStatus.PARTIALLY_FILLED);
                buy.setMessage("Partially filled. Waiting for remaining quantity");
            }

            if (sell.getExecutedQuantity() == sell.getQuantity()) {
                sell.setStatus(OrderStatus.FILLED);
                sell.setMessage("Order fully executed");
            } else {
                sell.setStatus(OrderStatus.PARTIALLY_FILLED);
                sell.setMessage("Partially filled. Waiting for remaining quantity");
            }

            recordTrade(buy, sell, matchedQty);

            orderRepository.save(buy);
            orderRepository.save(sell);

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

    // ================= ORDER BOOK SNAPSHOT =================

    public synchronized OrderBookResponse getOrderBookSnapshot(int depth) {

        Map<Double, Long> bidLevels =
                new TreeMap<>(Comparator.reverseOrder());

        Map<Double, Long> askLevels =
                new TreeMap<>();

        for (Order order : buyBook) {
            bidLevels.merge(
                    order.getPrice(),
                    order.getQuantity(),
                    Long::sum
            );
        }

        for (Order order : sellBook) {
            askLevels.merge(
                    order.getPrice(),
                    order.getQuantity(),
                    Long::sum
            );
        }

        List<OrderBookLevel> bids =
                bidLevels.entrySet()
                        .stream()
                        .limit(depth)
                        .map(e -> new OrderBookLevel(
                                e.getKey(),
                                e.getValue()
                        ))
                        .collect(Collectors.toList());

        List<OrderBookLevel> asks =
                askLevels.entrySet()
                        .stream()
                        .limit(depth)
                        .map(e -> new OrderBookLevel(
                                e.getKey(),
                                e.getValue()
                        ))
                        .collect(Collectors.toList());

        return new OrderBookResponse(bids, asks);
    }

    // ================= MARKET SUMMARY (O(1)) =================

    public synchronized Double getBestBid() {
        return buyBook.isEmpty() ? null : buyBook.peek().getPrice();
    }

    public synchronized Double getBestAsk() {
        return sellBook.isEmpty() ? null : sellBook.peek().getPrice();
    }

    public synchronized Double getSpread() {

        if (buyBook.isEmpty() || sellBook.isEmpty()) {
            return null;
        }

        return sellBook.peek().getPrice() - buyBook.peek().getPrice();
    }

}