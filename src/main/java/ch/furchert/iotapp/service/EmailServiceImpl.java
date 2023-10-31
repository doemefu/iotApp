package ch.furchert.iotapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

@Component
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender emailSender;

    public void sendSimpleMessage(String to, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("tbz.flugi@hotmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);

    }

    @Override
    public void sendSimpleMessageUsingTemplate(String to, String subject, String... templateModel) {

    }

    @Override
    public void sendMessageWithAttachment(String to, String subject, String text, String pathToAttachment) {

    }

    //https://www.thymeleaf.org/doc/articles/springmail.html
    @Override
    public void sendMessageUsingThymeleafTemplate(String to, String subject, Map<String, Object> templateModel) throws IOException, MessagingException {

    }
}
