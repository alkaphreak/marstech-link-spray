package fr.marstech.mtlinkspray.conf

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import fr.marstech.mtlinkspray.service.DigitalOceanSpacesServiceImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.web.server.ResponseStatusException
import software.amazon.awssdk.services.s3.S3Client

class S3ConfigTest {

    private val runner = ApplicationContextRunner()
        .withUserConfiguration(S3Config::class.java)
        .withPropertyValues(
            "mt.link-spray.storage.do-spaces.region=tor1",
            "mt.link-spray.storage.do-spaces.access-key=k",
            "mt.link-spray.storage.do-spaces.secret-key=s",
        )

    @Test
    fun noClientWhenEndpointIsBlank() {
        runner.withPropertyValues("mt.link-spray.storage.do-spaces.endpoint=")
            .run { assertFalse(it.containsBean("s3Client")) }
    }

    @Test
    fun clientWhenEndpointIsSet() {
        runner.withPropertyValues("mt.link-spray.storage.do-spaces.endpoint=https://tor1.digitaloceanspaces.com")
            .run { assertTrue(it.getBeansOfType(S3Client::class.java).size == 1) }
    }

    @Test
    fun storageServiceStartsWithoutClientAndAnswers503() {
        runner.withUserConfiguration(DigitalOceanSpacesServiceImpl::class.java)
            .withPropertyValues(
                "mt.link-spray.storage.do-spaces.endpoint=",
                "mt.link-spray.storage.do-spaces.bucket=b",
            )
            .run {
                val service = it.getBean(DigitalOceanSpacesServiceImpl::class.java)
                val ex = assertThrows<ResponseStatusException> { service.delete("k") }
                assertEquals(503, ex.statusCode.value())
            }
    }
}
