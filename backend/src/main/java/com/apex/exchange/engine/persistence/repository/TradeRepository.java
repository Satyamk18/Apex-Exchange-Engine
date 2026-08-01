package com.apex.exchange.engine.persistence.repository;

import com.apex.exchange.engine.persistence.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<TradeEntity, String> {

    List<TradeEntity> findBySymbol(String symbol);

    List<TradeEntity> findByBuyOrderIdOrSellOrderId(String buyOrderId, String sellOrderId);
}
