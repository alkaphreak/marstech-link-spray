package fr.marstech.mtlinkspray.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

/**
 * Sends notification mails. SMTP is optional (MLS-263): without `spring.mail.username` mails are
 * skipped with a warning, so the calling feature (abuse report) still succeeds.
 */
@Service
class MailSenderServiceImpl(
    private val mailSender: JavaMailSender,
    @param:Value($$"${mt.link-spray.notification.mail.receiver:}") private val receiver: String,
    @param:Value($$"${mt.link-spray.notification.mail.sender:}") private val sender: String,
    @param:Value($$"${spring.mail.username:}") smtpUser: String,
) : MailSenderService {

    private val enabled = smtpUser.isNotBlank()

    init {
        if (!enabled) log.warn("Mail disabled: LINK_SPRAY_SMTP_USER is not set, notifications are skipped")
    }

    override fun sendMail(
        subject: String,
        body: String
    ) {
        if (!enabled) {
            log.warn("Mail not sent (SMTP not configured): {}", subject)
            return
        }
        mailSender.send(SimpleMailMessage().apply {
            from = sender
            setTo(receiver)
            this.subject = subject
            text = body
        })
    }

    companion object {
        private val log = LoggerFactory.getLogger(MailSenderServiceImpl::class.java)
    }
}
