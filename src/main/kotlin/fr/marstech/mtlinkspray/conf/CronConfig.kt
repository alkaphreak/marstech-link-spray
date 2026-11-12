package fr.marstech.mtlinkspray.conf

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.http.HttpClient
import java.time.Duration

@Configuration
class CronConfig {
    // Redirects are not followed: a public URL redirecting to a private one would bypass the SSRF check.
    @Bean
    fun cronHttpClient(): HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .followRedirects(HttpClient.Redirect.NEVER)
        .build()
}
