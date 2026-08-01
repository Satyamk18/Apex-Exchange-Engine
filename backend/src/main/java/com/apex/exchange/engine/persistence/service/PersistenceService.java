package com.apex.exchange.engine.persistence.service;

import com.apex.exchange.engine.model.Trade;
import com.apex.exchange.engine.model.OrderStatusResponse;
import com.apex.exchange.engine.persistence.entity.OrderEntity;
import com.apex.exchange.engine.persistence.entity.TradeEntity;
import com.apex.exchange.engine.persistence.repository.OrderRepository;
import com.apex.exchange.engine.persistence.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersistenceService {

    private static final Logger log = LoggerFactory.getLogger(PersistenceService.class);

    private final TradeRepository tradeRepository;
    private final OrderRepository orderRepository;

    public PersistenceService(TradeRepository tradeRepository, OrderRepository orderRepository) {
        this.tradeRepository = tradeRepository;
        this.orderRepository = orderRepository;
    }

    @Async
    @Transactional
    public void saveTrade(Trade trade) {
        try {
            TradeEntity entity = new TradeEntity(
                    trade.getTradeId(),
                    trade.getSymbol(),
                    trade.getPrice(),
                    trade.getQuantity(),
                    trade.getBuyOrderId(),
                    trade.getSellOrderId(),
                    trade.getTimestamp()
            );
            tradeRepository.save(entity);
        } catch (Exception e) {
            log.error("Failed to persist trade {}: {}", trade.getTradeId(), e.getMessage());
        }
    }

    @Async
    @Transactional
    public void saveOrUpdateOrder(OrderStatusResponse orderStatus) {
        try {
            OrderEntity entity = new OrderEntity(
                    orderStatus.getOrderId(),
                    orderStatus.getSymbol(),
                    orderStatus.getSide(),
                    orderStatus.getType(),
                    orderStatus.getPrice(),
                    orderStatus.getInitialQuantity(),
                    orderStatus.getExecutedQuantity(),
                    orderStatus.getStatus(),
                    orderStatus.getTimestamp()
            );
            orderRepository.save(entity);
        } catch (Exception e) {
            log.error("Failed to persist order {}: {}", orderStatus.getOrderId(), e.getMessage());
        }
    }
}
