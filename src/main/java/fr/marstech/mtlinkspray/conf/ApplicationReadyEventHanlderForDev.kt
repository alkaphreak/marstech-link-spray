package fr.marstech.mtlinkspray.conf;

import fr.marstech.mtlinkspray.entity.HistoryItem;
import fr.marstech.mtlinkspray.entity.MtLinkSprayCollectionItem;
import fr.marstech.mtlinkspray.repository.MtLinkSprayCollectionRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.text.MessageFormat.format;
import static java.util.concurrent.TimeUnit.SECONDS;

@EnableAsync
@Configuration
@Log
@Profile("dev")
public class ApplicationReadyEventHanlderForDev {

  final MtLinkSprayCollectionRepository mtLinkSprayCollectionRepository;

  private final Environment environment;

  @Value("${mt.link-spray.version}")
  private String mtLinkSprayVersion;

  public ApplicationReadyEventHanlderForDev(
      Environment environment, MtLinkSprayCollectionRepository mtLinkSprayCollectionRepository) {
    this.environment = environment;
    this.mtLinkSprayCollectionRepository = mtLinkSprayCollectionRepository;
  }

  @EventListener(ApplicationReadyEvent.class)
  private void displayServerUrlInConsole() {
    Thread.ofVirtual()
        .start(
            () -> {
              try {
                SECONDS.sleep(3);
              } catch (InterruptedException e) {
                log.warning("Server URL display interrupted:" + e.getMessage());
                Thread.currentThread().interrupt();
              }
              log.info(
                  format(
                      "Local server : http://localhost:{0}",
                      environment.getProperty("server.port")));
            });
  }

  @EventListener(ApplicationReadyEvent.class)
  private void testMongoDbConnection() {
    Thread.ofVirtual()
        .start(
            () -> {
              try {
                SECONDS.sleep(4);

                String mongoDbUriEnvVar = environment.getProperty("spring.data.mongodb.uri");
                log.info(format("MongoDB URI : {0}", mongoDbUriEnvVar));

                String uuid = UUID.randomUUID().toString();
                MtLinkSprayCollectionItem item =
                    new MtLinkSprayCollectionItem(
                        uuid,
                        LocalDateTime.now(),
                        null,
                        true,
                        "Test item for MongoDB connection",
                        Map.of(),
                        new HistoryItem(),
                        List.of());

                mtLinkSprayCollectionRepository.save(item);
                assert mtLinkSprayCollectionRepository.findById(uuid).isPresent();

                mtLinkSprayCollectionRepository.findAll().stream()
                    .map(MtLinkSprayCollectionItem::toString)
                    .forEach(log::info);

              } catch (InterruptedException e) {
                log.warning("MongoDB connection test interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
              }
              log.info("MongoDB connection test");
            });
  }

  @EventListener(ApplicationReadyEvent.class)
  private void displayAppVersion() {
    Thread.ofVirtual()
        .start(
            () -> {
              try {
                SECONDS.sleep(5);
              } catch (InterruptedException e) {
                log.warning("App version display interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
              }
              log.info("App version : %s".formatted(mtLinkSprayVersion));
            });
  }
}
