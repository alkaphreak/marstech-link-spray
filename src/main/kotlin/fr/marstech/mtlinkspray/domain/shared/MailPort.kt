package fr.marstech.mtlinkspray.domain.shared

/** Sends an alert mail to the operator, implemented by `adapter/outbound/mail/SmtpMailSender` (ADR 0007). */
fun interface MailPort {

    fun sendMail(subject: String, body: String)
}
