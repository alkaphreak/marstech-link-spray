package fr.marstech.mtlinkspray.service

import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.web.client.ExpectedCount.never
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient

class CloudflareCachePurgeServiceImplTest {

    private val restClientBuilder = RestClient.builder()
    private val server = MockRestServiceServer.bindTo(restClientBuilder).build()

    private fun givenService(zoneId: String = "zone-1", apiToken: String = "token-1") =
        CloudflareCachePurgeServiceImpl(zoneId, apiToken, "https://sol4.space/", restClientBuilder)

    @Test
    fun purgeShouldPostPublicUrlToCloudflare() {
        val service = givenService()
        server.expect(requestTo("https://api.cloudflare.com/client/v4/zones/zone-1/purge_cache"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", "Bearer token-1"))
            .andExpect(content().json("""{"files":["https://sol4.space/api/images/img-1"]}"""))
            .andRespond(withSuccess())

        service.purge("/api/images/img-1")

        server.verify()
    }

    @Test
    fun purgeShouldNotThrowWhenCloudflareFails() {
        val service = givenService()
        server.expect(requestTo("https://api.cloudflare.com/client/v4/zones/zone-1/purge_cache"))
            .andRespond(withStatus(HttpStatus.FORBIDDEN))

        service.purge("/api/images/img-1")

        server.verify()
    }

    @Test
    fun purgeShouldDoNothingWhenNotConfigured() {
        val service = givenService(apiToken = "")
        server.expect(never(), requestTo("https://api.cloudflare.com/client/v4/zones/zone-1/purge_cache"))

        service.purge("/api/images/img-1")

        server.verify()
    }
}
