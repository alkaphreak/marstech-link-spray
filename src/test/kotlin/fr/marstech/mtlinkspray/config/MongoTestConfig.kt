package fr.marstech.mtlinkspray.config

import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Configuration
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName

@Configuration
open class MongoTestConfig {

    @Container
    @ServiceConnection
    var mongoDBContainer: MongoDBContainer? =
        MongoDBContainer(DockerImageName.parse(TestConfig.MONGO_DB_DOCKER_IMAGE_NAME)).withReuse(true)
}
