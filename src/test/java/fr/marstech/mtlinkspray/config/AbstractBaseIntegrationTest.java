package fr.marstech.mtlinkspray.config;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
public abstract class AbstractBaseIntegrationTest {

    @Bean
    private MongoClient newMongoClient() {
        return MongoClients.create(new ConnectionString(mongoDBContainer.getConnectionString()));
    }

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0.4")
            .withExposedPorts(27017).withReuse(true);

    @DynamicPropertySource
    static void containersProperties(DynamicPropertyRegistry registry) {
        mongoDBContainer.start();
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
}