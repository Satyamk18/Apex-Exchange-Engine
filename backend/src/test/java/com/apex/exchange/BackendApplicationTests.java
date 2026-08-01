package com.apex.exchange;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        properties = {
                "spring.kafka.bootstrap-servers=localhost:19092",
                "spring.data.redis.host=localhost",
                "spring.datasource.url=jdbc:h2:mem:testctx;DB_CLOSE_DELAY=-1",
                "spring.jpa.hibernate.ddl-auto=create-drop"
        }
)
class BackendApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the application context assembles without errors
    }
}
