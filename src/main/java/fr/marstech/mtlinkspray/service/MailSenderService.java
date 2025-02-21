package fr.marstech.mtlinkspray.service;

public interface MailSenderService {

    void sendMail(String subject, String body);
}
