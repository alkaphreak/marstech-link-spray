package fr.marstech.mtlinkspray.service

interface MailSenderService {
    fun sendMail(subject: String, body: String)
}
