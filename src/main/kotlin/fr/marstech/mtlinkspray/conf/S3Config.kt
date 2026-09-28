package fr.marstech.mtlinkspray.conf

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

/**
 * Configures the S3 client used to talk to the Digital Ocean Spaces bucket backing the
 * image storage service (MLS-203). Digital Ocean Spaces is S3-compatible, so the standard
 * AWS SDK v2 client is used with a custom endpoint override.
 */
@Configuration
class S3Config(
    @Value("\${mt.link-spray.storage.do-spaces.endpoint}") private val endpoint: String,
    @Value("\${mt.link-spray.storage.do-spaces.region}") private val region: String,
    @Value("\${mt.link-spray.storage.do-spaces.access-key}") private val accessKey: String,
    @Value("\${mt.link-spray.storage.do-spaces.secret-key}") private val secretKey: String,
) {

    @Bean
    fun s3Client(): S3Client = S3Client.builder()
        .endpointOverride(URI.create(endpoint))
        .region(Region.of(region))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
        )
        .build()
}
