package fr.marstech.mtlinkspray.config;

import jakarta.mail.internet.MimeMessage;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class JavaMailSenderTestConfiguration {

    @Bean
    JavaMailSender mockMailSender() {
        MimeMessage mimeMessage = Mockito.mock(MimeMessage.class);
        JavaMailSender javaMailSender = Mockito.mock(JavaMailSender.class);
        Mockito.when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        return javaMailSender;
    }
}

