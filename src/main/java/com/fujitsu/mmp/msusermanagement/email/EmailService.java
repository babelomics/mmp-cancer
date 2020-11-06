package com.fujitsu.mmp.msusermanagement.email;

import com.fujitsu.mmp.msusermanagement.dto.UserRegistryRequestDTO;
import org.springframework.http.ResponseEntity;

public interface EmailService {

    public void sendSimpleMessage(String to, String subject, String text);

    public void sendHTMLMessage(String to, String subject, String text);

    public void sendMessageWithAttachment(String to, String subject, String text, String pathToAttachment, String content);

    ResponseEntity<?> sendLinkToPasswordScreen (String identifierOrEmail);

    public void sendAccessRequestEmail(UserRegistryRequestDTO userRegistryRequestDTO);

}