package fr.marstech.mtlinkspray.service


fun interface MailSenderService {

    fun sendMail(subject: String, body: String)
}
