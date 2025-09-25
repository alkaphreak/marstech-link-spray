package fr.marstech.mtlinkspray.config

import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender

@Configuration
open class JavaMailSenderTestConfiguration {
    @Bean
    open fun mockMailSender(): JavaMailSender {
        val javaMailSender = Mockito.mock<JavaMailSender>(JavaMailSender::class.java)
        Mockito.`when`<MimeMessage?>(javaMailSender.createMimeMessage())
            .thenAnswer(Answer { _: InvocationOnMock? ->
                val mockSession = Mockito.mock<Session?>(Session::class.java)
                MimeMessage(mockSession)
            })
        return javaMailSender
    }
}
