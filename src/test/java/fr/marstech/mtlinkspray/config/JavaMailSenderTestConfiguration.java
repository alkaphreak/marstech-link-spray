package fr.marstech.mtlinkspray.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class JavaMailSenderTestConfiguration {

    @Bean
    JavaMailSender mockMailSender() {
        JavaMailSender javaMailSender = Mockito.mock(JavaMailSender.class);
        Mockito.when(javaMailSender.createMimeMessage()).thenAnswer(invocation -> new jakarta.mail.internet.MimeMessage((jakarta.mail.Session) null));
        return javaMailSender;
    }
}
