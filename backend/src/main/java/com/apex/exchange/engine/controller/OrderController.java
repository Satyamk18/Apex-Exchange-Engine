package com.apex.exchange.engine.controller;

import com.apex.exchange.engine.kafka.OrderProducer;
import com.apex.exchange.engine.model.OrderEvent;
import com.apex.exchange.engine.model.OrderRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderProducer orderProducer;

    public OrderController(OrderProducer orderProducer) {
        this.orderProducer = orderProducer;
    }

    @PostMapping
    public String placeOrder(@RequestBody OrderRequest request) {

        OrderEvent event = new OrderEvent(
                request.getOrderId(),
                request.getSymbol(),
                request.getSide(),
                request.getPrice(),
                request.getQuantity(),
                System.nanoTime()
        );

        orderProducer.publishOrder(event);

        return "Order published to Kafka";
    }
}