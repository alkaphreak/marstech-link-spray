package fr.marstech.mtlinkspray.domain.shared
fun interface MailSenderService {

    fun sendMail(subject: String, body: String)
}
