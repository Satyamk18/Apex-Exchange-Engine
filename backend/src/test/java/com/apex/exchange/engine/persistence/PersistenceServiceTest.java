package com.apex.exchange.engine.persistence;

import com.apex.exchange.engine.model.*;
import com.apex.exchange.engine.persistence.entity.OrderEntity;
import com.apex.exchange.engine.persistence.entity.TradeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PersistenceServiceTest {

    private EntityManagerFactory emf;
    private EntityManager em;

    @BeforeAll
    void setup() {
        org.h2.jdbcx.JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
        ds.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        ds.setUser("sa");
        ds.setPassword("");

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(ds);
        factory.setPackagesToScan("com.apex.exchange.engine.persistence.entity");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProps = new Properties();
        jpaProps.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        jpaProps.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        jpaProps.setProperty("hibernate.show_sql", "false");
        factory.setJpaProperties(jpaProps);
        factory.afterPropertiesSet();

        emf = factory.getObject();
        em = emf.createEntityManager();
    }

    @AfterAll
    void teardown() {
        if (em != null) em.close();
        if (emf != null) emf.close();
    }

    private void saveInTx(Object entity) {
        em.getTransaction().begin();
        em.persist(entity);
        em.getTransaction().commit();
        em.clear();
    }

    @Test
    void testSaveTrade() {
        TradeEntity entity = new TradeEntity("t1", "AAPL", 150.0, 10, "b1", "s1", System.nanoTime());
        saveInTx(entity);

        List<TradeEntity> trades = em.createQuery(
                        "SELECT t FROM TradeEntity t WHERE t.symbol = :symbol", TradeEntity.class)
                .setParameter("symbol", "AAPL")
                .getResultList();

        assertEquals(1, trades.size());
        TradeEntity saved = trades.get(0);
        assertEquals("t1", saved.getTradeId());
        assertEquals("AAPL", saved.getSymbol());
        assertEquals(150.0, saved.getPrice());
        assertEquals(10, saved.getQuantity());
        assertEquals("b1", saved.getBuyOrderId());
        assertEquals("s1", saved.getSellOrderId());

        em.getTransaction().begin();
        em.createQuery("DELETE FROM TradeEntity t WHERE t.symbol = 'AAPL'").executeUpdate();
        em.getTransaction().commit();
    }

    @Test
    void testSaveAndQueryOrderByStatus() {
        OrderEntity filledOrder = new OrderEntity(
                "o1", "TSLA", OrderSide.BUY, OrderType.LIMIT,
                200.0, 20, 20, OrderStatus.FILLED, System.nanoTime()
        );
        OrderEntity newOrder = new OrderEntity(
                "o2", "TSLA", OrderSide.SELL, OrderType.LIMIT,
                210.0, 10, 0, OrderStatus.NEW, System.nanoTime()
        );
        saveInTx(filledOrder);
        saveInTx(newOrder);

        List<OrderEntity> tslaOrders = em.createQuery(
                        "SELECT o FROM OrderEntity o WHERE o.symbol = :symbol", OrderEntity.class)
                .setParameter("symbol", "TSLA")
                .getResultList();
        assertEquals(2, tslaOrders.size());

        List<OrderEntity> filledOrders = em.createQuery(
                        "SELECT o FROM OrderEntity o WHERE o.status = :status", OrderEntity.class)
                .setParameter("status", OrderStatus.FILLED)
                .getResultList();
        assertEquals(1, filledOrders.size());
        assertEquals("o1", filledOrders.get(0).getOrderId());

        em.getTransaction().begin();
        em.createQuery("DELETE FROM OrderEntity o WHERE o.symbol = 'TSLA'").executeUpdate();
        em.getTransaction().commit();
    }

    @Test
    void testFindTradesByBuyOrSellOrder() {
        saveInTx(new TradeEntity("t10", "MSFT", 300.0, 5, "buy-1", "sell-1", System.nanoTime()));
        saveInTx(new TradeEntity("t11", "MSFT", 301.0, 10, "buy-2", "sell-1", System.nanoTime()));
        saveInTx(new TradeEntity("t12", "MSFT", 299.0, 3, "buy-3", "sell-2", System.nanoTime()));

        List<TradeEntity> tradesForSell1 = em.createQuery(
                        "SELECT t FROM TradeEntity t WHERE t.buyOrderId = :oid OR t.sellOrderId = :oid",
                        TradeEntity.class)
                .setParameter("oid", "sell-1")
                .getResultList();
        assertEquals(2, tradesForSell1.size());

        em.getTransaction().begin();
        em.createQuery("DELETE FROM TradeEntity t WHERE t.symbol = 'MSFT'").executeUpdate();
        em.getTransaction().commit();
    }
}
