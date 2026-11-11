package fr.marstech.mtlinkspray.service

import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender

class MailSenderServiceImplTest {

    private val mailSender = mock(JavaMailSender::class.java)

    @Test
    fun skipsSendingWhenSmtpUserIsBlank() {
        MailSenderServiceImpl(mailSender, "to@x", "from@x", "").sendMail("s", "b")
        verifyNoInteractions(mailSender)
    }

    @Test
    fun sendsWhenSmtpUserIsSet() {
        MailSenderServiceImpl(mailSender, "to@x", "from@x", "user").sendMail("s", "b")
        verify(mailSender).send(any<SimpleMailMessage>())
    }
}
