package fr.marstech.mtlinkspray.adapter.outbound.mail
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender

class SmtpMailSenderTest {

    private val mailSender = mock(JavaMailSender::class.java)

    @Test
    fun skipsSendingWhenSmtpUserIsBlank() {
        SmtpMailSender(mailSender, "to@x", "from@x", "").sendMail("s", "b")
        verifyNoInteractions(mailSender)
    }

    @Test
    fun sendsWhenSmtpUserIsSet() {
        SmtpMailSender(mailSender, "to@x", "from@x", "user").sendMail("s", "b")
        verify(mailSender).send(any<SimpleMailMessage>())
    }
}
