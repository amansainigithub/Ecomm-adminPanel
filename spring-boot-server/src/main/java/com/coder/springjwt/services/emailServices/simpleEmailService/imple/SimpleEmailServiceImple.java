package com.coder.springjwt.services.emailServices.simpleEmailService.imple;

import com.coder.springjwt.payload.emailPayloads.EmailDetailsPayload;
import com.coder.springjwt.services.emailServices.simpleEmailService.SimpleEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SimpleEmailServiceImple implements SimpleEmailService {

    @Value("${spring.mail.username}")
    private String sender;

    @Autowired
    private JavaMailSender javaMailSender;


    public String sendSimpleMail(EmailDetailsPayload emailDetailsPayload)
    {
        // Try block to check for exceptions
        try {
            // Creating a simple mail message
            SimpleMailMessage mailMessage
                    = new SimpleMailMessage();

            // Setting up necessary details
            mailMessage.setFrom(sender);
            mailMessage.setTo(emailDetailsPayload.getRecipient());
            mailMessage.setText(emailDetailsPayload.getMsgBody());
            mailMessage.setSubject(emailDetailsPayload.getSubject());

            // Sending the mail
            javaMailSender.send(mailMessage);
            return "Mail Sent Successfully...";
        }

        // Catch block to handle the exceptions
        catch (Exception e) {
            return "Error while Sending Mail";
        }
    }
}
