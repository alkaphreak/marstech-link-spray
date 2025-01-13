package fr.marstech.mtlinkspray.conf;

import fr.marstech.mtlinkspray.entity.MtLinkSprayCollectionItem;
import fr.marstech.mtlinkspray.repository.MtLinkSprayCollectionRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static java.text.MessageFormat.format;
import static java.util.concurrent.TimeUnit.SECONDS;

@EnableAsync
@Configuration
@Log
@Profile("dev")
public class PostConstructConfForDev {

    final MtLinkSprayCollectionRepository mtLinkSprayCollectionRepository;

    private final Environment environment;

    public PostConstructConfForDev(Environment environment, MtLinkSprayCollectionRepository mtLinkSprayCollectionRepository) {
        this.environment = environment;
        this.mtLinkSprayCollectionRepository = mtLinkSprayCollectionRepository;
    }

    @PostConstruct
    private void displayServerUrlInConsole() {
        CompletableFuture.runAsync(() -> {
            try {
                SECONDS.sleep(3);
            } catch (InterruptedException e) {
                // Do nothing because we don't care.
            }

            log.info(format("Local server : http://localhost:{0}", environment.getProperty("server.port")));
        });
    }

    @PostConstruct
    private void testMongoDbConnection() {
        CompletableFuture.runAsync(() -> {
            try {
                SECONDS.sleep(4);

                String mongoDbUriEnvVar = environment.getProperty("spring.data.mongodb.uri");
                log.info(format("MongoDB URI : {0}", mongoDbUriEnvVar));

                String uuid = UUID.randomUUID().toString();
                mtLinkSprayCollectionRepository.save(
                        new MtLinkSprayCollectionItem(
                                uuid,
                                format("Description : {0}", uuid),
                                LocalDateTime.now()
                        )
                );
                assert mtLinkSprayCollectionRepository.findById(uuid).isPresent();
                //mtLinkSprayCollectionRepository.deleteById(uuid);
                mtLinkSprayCollectionRepository.findAll().forEach(it -> log.info(it.toString()));
            } catch (InterruptedException e) {
                // Do nothing because we don't care.
            }

            log.info("MongoDB connection test");
        });
    }
}
