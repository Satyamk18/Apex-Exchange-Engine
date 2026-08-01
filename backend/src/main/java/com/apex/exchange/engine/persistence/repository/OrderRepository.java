package com.apex.exchange.engine.persistence.repository;

import com.apex.exchange.engine.persistence.entity.OrderEntity;
import com.apex.exchange.engine.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, String> {

    List<OrderEntity> findBySymbol(String symbol);

    List<OrderEntity> findByStatus(OrderStatus status);

    List<OrderEntity> findBySymbolAndStatus(String symbol, OrderStatus status);
}
