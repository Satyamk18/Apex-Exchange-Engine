package com.apex.exchange.engine.controller;

import com.apex.exchange.engine.persistence.entity.OrderEntity;
import com.apex.exchange.engine.persistence.entity.TradeEntity;
import com.apex.exchange.engine.persistence.repository.OrderRepository;
import com.apex.exchange.engine.persistence.repository.TradeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/history")
public class HistoryController {

    private final TradeRepository tradeRepository;
    private final OrderRepository orderRepository;

    public HistoryController(TradeRepository tradeRepository, OrderRepository orderRepository) {
        this.tradeRepository = tradeRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/trades/{symbol}")
    public ResponseEntity<List<TradeEntity>> getTradesBySymbol(@PathVariable String symbol) {
        return ResponseEntity.ok(tradeRepository.findBySymbol(symbol));
    }

    @GetMapping("/trades/order/{orderId}")
    public ResponseEntity<List<TradeEntity>> getTradesByOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(tradeRepository.findByBuyOrderIdOrSellOrderId(orderId, orderId));
    }

    @GetMapping("/orders/{symbol}")
    public ResponseEntity<List<OrderEntity>> getOrdersBySymbol(@PathVariable String symbol) {
        return ResponseEntity.ok(orderRepository.findBySymbol(symbol));
    }

    @GetMapping("/orders/{symbol}/{orderId}")
    public ResponseEntity<OrderEntity> getOrderById(@PathVariable String symbol, @PathVariable String orderId) {
        return orderRepository.findById(orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
