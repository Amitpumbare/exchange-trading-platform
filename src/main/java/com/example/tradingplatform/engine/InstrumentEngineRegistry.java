package com.example.tradingplatform.engine;

import com.example.tradingplatform.repository.OrderRepository;
import com.example.tradingplatform.repository.TradeRepository;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class InstrumentEngineRegistry {

    private final ConcurrentHashMap<Long, OrderMatchingEngine> engines =
            new ConcurrentHashMap<>();

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;

    public InstrumentEngineRegistry(OrderRepository orderRepository,
                                    TradeRepository tradeRepository) {
        this.orderRepository = orderRepository;
        this.tradeRepository = tradeRepository;
    }

    public OrderMatchingEngine getEngine(Long instrumentId) {
        return engines.computeIfAbsent(
                instrumentId,
                id -> new OrderMatchingEngine(
                        id,
                        orderRepository,
                        tradeRepository
                )
        );
    }
}
