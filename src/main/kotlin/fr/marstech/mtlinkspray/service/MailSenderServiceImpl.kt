package fr.marstech.mtlinkspray.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailSenderServiceImpl implements MailSenderService {

    private final JavaMailSender mailSender;

    @Value("${mt.link-spray.notification.mail.receiver:}")
    private String receiver;

    @Value("${mt.link-spray.notification.mail.sender:}")
    private String sender;

    @Autowired
    public MailSenderServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendMail(String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(receiver);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
