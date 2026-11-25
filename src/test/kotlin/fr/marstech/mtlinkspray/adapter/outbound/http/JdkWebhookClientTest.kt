package fr.marstech.mtlinkspray.adapter.outbound.http

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.http.HttpClient
import java.time.Duration
import java.util.Optional

class JdkWebhookClientTest {

    @Test
    fun shouldNeverFollowRedirectsAndBoundTheConnection() {
        // Given / When
        val http = JdkWebhookClient().http

        // Then: a public URL redirecting to a private one would bypass the SSRF check
        assertEquals(HttpClient.Redirect.NEVER, http.followRedirects())
        assertEquals(Optional.of(Duration.ofSeconds(5)), http.connectTimeout())
    }
}
