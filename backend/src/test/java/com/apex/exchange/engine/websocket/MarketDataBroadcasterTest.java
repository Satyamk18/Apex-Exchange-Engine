package com.apex.exchange.engine.websocket;

import com.apex.exchange.engine.model.OrderBookDepthDto;
import com.apex.exchange.engine.model.TickerDto;
import com.apex.exchange.engine.model.TradeEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class MarketDataBroadcasterTest {

    @Test
    void testBroadcastTradeAndTicker() {
        SimpMessagingTemplate messagingTemplate = Mockito.mock(SimpMessagingTemplate.class);
        MarketDataBroadcaster broadcaster = new MarketDataBroadcaster(messagingTemplate);

        TradeEvent trade = new TradeEvent("t1", "AAPL", "b1", "s1", 150.0, 10, System.currentTimeMillis());

        broadcaster.broadcastTrade(trade);

        verify(messagingTemplate).convertAndSend(eq("/topic/trades/AAPL"), eq(trade));
        verify(messagingTemplate).convertAndSend(eq("/topic/ticker/AAPL"), any(TickerDto.class));
    }

    @Test
    void testBroadcastDepth() {
        SimpMessagingTemplate messagingTemplate = Mockito.mock(SimpMessagingTemplate.class);
        MarketDataBroadcaster broadcaster = new MarketDataBroadcaster(messagingTemplate);

        OrderBookDepthDto depth = new OrderBookDepthDto("TSLA", System.currentTimeMillis(), Collections.emptyList(), Collections.emptyList());

        broadcaster.broadcastDepth(depth);

        verify(messagingTemplate).convertAndSend(eq("/topic/depth/TSLA"), eq(depth));
    }
}
