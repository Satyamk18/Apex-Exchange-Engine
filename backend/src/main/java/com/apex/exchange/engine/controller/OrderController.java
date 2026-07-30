package com.apex.exchange.engine.controller;

import com.apex.exchange.engine.kafka.OrderProducer;
import com.apex.exchange.engine.model.OrderEvent;
import com.apex.exchange.engine.model.OrderRequest;
import com.apex.exchange.engine.model.OrderType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderProducer orderProducer;

    public OrderController(OrderProducer orderProducer) {
        this.orderProducer = orderProducer;
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
                request.getPrice(),
                request.getQuantity(),
                System.nanoTime()
        );

        orderProducer.publishOrder(event);

        return ResponseEntity.ok("Order published to Kafka");
    }
}