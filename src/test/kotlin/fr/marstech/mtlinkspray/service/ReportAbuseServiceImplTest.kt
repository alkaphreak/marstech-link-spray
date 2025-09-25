package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.config.TestConfig.MONGO_DB_DOCKER_IMAGE_NAME
import fr.marstech.mtlinkspray.repository.AbuseReportRepository
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
@SpringBootTest
internal class ReportAbuseServiceImplTest(
    val abuseReportRepository: AbuseReportRepository,
    val reportAbuseService: ReportAbuseServiceImpl
) {

    @BeforeEach
    fun setUp() {
        abuseReportRepository.deleteAll()
    }

    @Test
    fun reportAbuse() {
        val request = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(request.remoteAddr).thenReturn("127.0.0.1")

        val abuse = "abuse"
        reportAbuseService.reportAbuse(abuse, request)
        Assertions.assertEquals(1, abuseReportRepository.count())
        Assertions.assertEquals("reportAbuse:abuse", abuseReportRepository.findAll().first().description)
    }

    companion object {
        @Suppress("unused")
        @Container
        @ServiceConnection
        var mongoDBContainer: MongoDBContainer? =
            MongoDBContainer(DockerImageName.parse(MONGO_DB_DOCKER_IMAGE_NAME)).withReuse(true)
    }
}
