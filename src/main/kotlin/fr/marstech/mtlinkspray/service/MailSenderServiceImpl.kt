package fr.marstech.mtlinkspray.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class MailSenderServiceImpl(
    private val mailSender: JavaMailSender,
    @Value("\${mt.link-spray.notification.mail.receiver:}") private val receiver: String,
    @Value("\${mt.link-spray.notification.mail.sender:}") private val sender: String
) : MailSenderService {

    override fun sendMail(subject: String, body: String) {
        mailSender.send(SimpleMailMessage().apply {
            from = sender
            setTo(receiver)
            this.subject = subject
            text = body
        })
    }
}