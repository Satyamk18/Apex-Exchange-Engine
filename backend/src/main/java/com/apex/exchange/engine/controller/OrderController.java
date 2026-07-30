package com.apex.exchange.engine.controller;

import com.apex.exchange.engine.kafka.OrderProducer;
import com.apex.exchange.engine.model.*;
import com.apex.exchange.engine.service.MatchingEngineManager;
import com.apex.exchange.engine.service.OrderStatusTracker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderProducer orderProducer;
    private final MatchingEngineManager matchingEngineManager;
    private final OrderStatusTracker orderStatusTracker;

    public OrderController(OrderProducer orderProducer,
                           MatchingEngineManager matchingEngineManager,
                           OrderStatusTracker orderStatusTracker) {
        this.orderProducer = orderProducer;
        this.matchingEngineManager = matchingEngineManager;
        this.orderStatusTracker = orderStatusTracker;
    }

    @PostMapping
    public ResponseEntity<String> placeOrder(@RequestBody OrderRequest request) {

        OrderType type = request.getType() != null ? request.getType() : OrderType.LIMIT;

        if (type == OrderType.LIMIT && request.getPrice() <= 0) {
            return ResponseEntity.badRequest().body("Limit orders must specify a price greater than 0");
        }

        OrderEvent event = new OrderEvent(
                request.getOrderId(),
                request.getSymbol(),
                request.getSide(),
                type,
                OrderAction.CREATE,
                request.getPrice(),
                request.getQuantity(),
                System.nanoTime()
        );

        orderProducer.publishOrder(event);

        return ResponseEntity.ok("Order published to Kafka");
    }

    @DeleteMapping("/{symbol}/{orderId}")
    public ResponseEntity<String> cancelOrder(@PathVariable String symbol, @PathVariable String orderId) {

        OrderEvent cancelEvent = new OrderEvent(
                orderId,
                symbol,
                null,
                OrderType.LIMIT,
                OrderAction.CANCEL,
                0.0,
                0,
                System.nanoTime()
        );

        orderProducer.publishOrder(cancelEvent);

        return ResponseEntity.ok("Order cancellation published to Kafka");
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderStatusResponse> getOrderStatus(@PathVariable String orderId) {
        return orderStatusTracker.getOrderStatus(orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/book/{symbol}")
    public ResponseEntity<OrderBookDepthDto> getOrderBookDepth(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "10") int depth) {

        OrderBookDepthDto depthDto = matchingEngineManager.getOrderBookDepth(symbol, depth);
        return ResponseEntity.ok(depthDto);
    }
}