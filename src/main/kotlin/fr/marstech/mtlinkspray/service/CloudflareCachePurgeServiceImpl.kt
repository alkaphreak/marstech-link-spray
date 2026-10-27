package fr.marstech.mtlinkspray.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

/**
 * Purges single URLs through the Cloudflare API (`POST /zones/{zone_id}/purge_cache`), with a
 * token scoped to `Zone.Cache Purge` (MLS-237). Disabled when the zone id or token is unset.
 */
@Service
class CloudflareCachePurgeServiceImpl(
    @Value("\${mt.link-spray.cloudflare.zone-id:}") private val zoneId: String,
    @Value("\${mt.link-spray.cloudflare.api-token:}") private val apiToken: String,
    @Value("\${mt.link-spray.cloudflare.public-base-url:https://sol4.space}") private val publicBaseUrl: String,
    restClientBuilder: RestClient.Builder = RestClient.builder(),
) : CachePurgeService {

    private val restClient = restClientBuilder.baseUrl("https://api.cloudflare.com/client/v4").build()

    override fun purge(path: String) {
        if (zoneId.isBlank() || apiToken.isBlank()) {
            log.debug("Cloudflare cache purge disabled, {} not purged", path)
            return
        }
        val url = publicBaseUrl.trimEnd('/') + path
        runCatching {
            restClient.post()
                .uri("/zones/{zoneId}/purge_cache", zoneId)
                .header(AUTHORIZATION, "Bearer $apiToken")
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapOf("files" to listOf(url)))
                .retrieve()
                .toBodilessEntity()
        }.onFailure { log.warn("Cloudflare cache purge failed for {}: {}", url, it.message) }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(CloudflareCachePurgeServiceImpl::class.java)
    }
}
