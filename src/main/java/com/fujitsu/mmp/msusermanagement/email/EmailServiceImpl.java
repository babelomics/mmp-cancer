package com.fujitsu.mmp.msusermanagement.email;

import com.fujitsu.mmp.msusermanagement.dto.UserRegistryRequestDTO;
import com.fujitsu.mmp.msusermanagement.entities.User;
import com.fujitsu.mmp.msusermanagement.mappers.UserMapper;
import com.fujitsu.mmp.msusermanagement.model.response.MessageResponse;
import com.fujitsu.mmp.msusermanagement.repositories.UserRepository;
import com.fujitsu.mmp.msusermanagement.utility.JWTUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;

@Service("EmailService")
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JWTUtility jwtUtility;

    @Value("${mail.noreply.address}")
    private String NOREPLY_ADDRESS;

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(NOREPLY_ADDRESS);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            emailSender.send(message);
        } catch (MailException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void sendHTMLMessage(String to, String subject, String text) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");

            helper.setFrom(NOREPLY_ADDRESS);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);

            emailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessageWithAttachment(String to, String subject, String text, String pathToAttachment, String content) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(NOREPLY_ADDRESS);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            FileSystemResource file = new FileSystemResource(new File(pathToAttachment));
            helper.addAttachment("Invoice", file);

            emailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ResponseEntity<?> sendLinkToPasswordScreen(String identifierOrEmail) {
        User entity = null;
        if(identifierOrEmail == null){
            return new ResponseEntity<>(
                    "Error: Either email or user needs to be provided.",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if(userRepository.existsByIdentifier(identifierOrEmail)){
            entity = userRepository.findByIdentifier(identifierOrEmail);
        }else if (userRepository.existsByEmail(identifierOrEmail)){
            entity = userRepository.findByEmail(identifierOrEmail);
        } else {
            return new ResponseEntity<>(
                    "Error: The email or identifier provided does not exist in the system.",
                    HttpStatus.NOT_FOUND);
        }
        sendHTMLMessage(entity.getEmail(),"Your password has been reset","Hello "+entity.getIdentifier()
                +". Please follow the link below to set a new password: "+generateLink(entity.getIdentifier()));

        return ResponseEntity.ok(new MessageResponse("Email send correctly!"));
    }

    @Override
    public void sendAccessRequestEmail(UserRegistryRequestDTO userRegistryRequestDTO) {
        String text;
        String subject;
        if(userRegistryRequestDTO.getAttended().equals("approve")){
            subject = "Request approved";
            text = "Your access request has been approved: "+generateLink(userRegistryRequestDTO.getIdentifier());
        }else {
            if (userRegistryRequestDTO.getAccessRefusalReason() != null) {
                text = "Hello " + userRegistryRequestDTO.getIdentifier() + ", we regret to inform you that your application has been rejected.";
                subject = "Request rejected";
            } else {
                text = "Hello " + userRegistryRequestDTO.getIdentifier() + ", we regret to inform you that your application has been rejected due the following reasons: " + userRegistryRequestDTO.getAccessRefusalReason();
                subject = "Request rejected";
            }
        }
        sendHTMLMessage(userRegistryRequestDTO.getEmail(), subject, text);
    }

    private String generateLink(String identifier) {
        return "<a href=\"http://localhost:3000/mmp-cancer/new-password?token="+jwtUtility.generateTokenForLink(identifier)+"\">link</a>";
    }
}
