package fr.marstech.mtlinkspray.conf;

import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.text.MessageFormat.format;

@EnableAsync
@Configuration
@Log
public class PostConstructConf {

    private final Environment environment;

    public PostConstructConf(
            Environment environment
    ) {
        this.environment = environment;
    }

    @PostConstruct
    private void displayServerUrlInConsole() {
        CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                // Do nothing because we don't care.
            }

            log.info(format("Local server : http://localhost:{0}",
                    environment.getProperty("server.port"))
            );
        });
    }
}
