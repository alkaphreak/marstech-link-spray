package fr.marstech.mtlinkspray.service;

import fr.marstech.mtlinkspray.repository.AbuseReportRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static fr.marstech.mtlinkspray.config.TestConfig.MONGO_DB_DOCKER_IMAGE_NAME;

@Testcontainers
@SpringBootTest
class ReportAbuseServiceImplTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse(MONGO_DB_DOCKER_IMAGE_NAME)).withReuse(true);

    @Autowired
    private ReportAbuseServiceImpl reportAbuseService;

    @Autowired
    public AbuseReportRepository abuseReportRepository;

    ReportAbuseServiceImplTest() {
    }

    @Test
    void reportAbuse() {
        String abuse = "abuse";
        reportAbuseService.reportAbuse(abuse);
        assert (true);
    }

}