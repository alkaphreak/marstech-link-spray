package fr.marstech.e2e

import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.mail.javamail.JavaMailSender

/** The suite's own mail sink, so it never depends on a mock living in the production package tree. */
@TestConfiguration
class E2eMailConfiguration {
    @Bean
    @Primary
    fun e2eMailSender(): JavaMailSender = Mockito.mock(JavaMailSender::class.java).also { sender ->
        Mockito.`when`(sender.createMimeMessage()).thenAnswer { MimeMessage(Mockito.mock(Session::class.java)) }
    }
}
