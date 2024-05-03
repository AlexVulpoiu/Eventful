package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.enums.EventStatus;
import com.unibuc.fmi.eventful.model.AbstractUser;
import com.unibuc.fmi.eventful.model.Event;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class SendEmailService {

    @Value("${eventful.app.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${eventful.app.name}")
    private String senderName;

    private final JavaMailSender javaMailSender;

    public void sendVerificationEmail(AbstractUser user) throws MessagingException, UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String subject = "Please verify your registration";
        String content = "Dear [[name]],<br>"
                + "Please click the link below to verify your registration:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>"
                + "Thank you,<br>"
                + senderName;

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(senderEmail, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", user.getFullName());
        String verifyURL = frontendUrl + "/verify?code=" + user.getVerificationCode();

        content = content.replace("[[URL]]", verifyURL);

        helper.setText(content, true);

        javaMailSender.send(message);
    }

    public void sendEventStatusChanged(Event event) throws MessagingException, UnsupportedEncodingException {
        String toAddress = event.getOrganiser().getEmail();
        String subject = "Status changed for " + event.getName();
        String reason = EventStatus.REJECTED.equals(event.getStatus())
                ? "The reason for this decision is " + event.getRejectionReason() + ".<br>" : "";
        String content = "Dear [[NAME]],<br>"
                + "Your event - [[EVENT_NAME]] was [[STATUS]] by one of Eventful admins.<br>"
                + reason
                + "Thank you,<br>"
                + senderName;

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(senderEmail, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[NAME]]", event.getOrganiser().getFullName());
        content = content.replace("[[EVENT_NAME]]", event.getName());
        content = content.replace("[[STATUS]]", String.valueOf(event.getStatus()));

        helper.setText(content, true);

        javaMailSender.send(message);
    }
}
