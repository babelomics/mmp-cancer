package com.fujitsu.mmp.msusermanagement.controllers;

import com.fujitsu.mmp.msusermanagement.email.EmailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600, exposedHeaders = "Authorization")
@RequestMapping("/api/email")
@RestController
public class EmailController {

    @Autowired
    public EmailServiceImpl emailService;

    /**
     * Send an email to a person who has forgotten their password
     * @param identifierOrEmail User identifier or email
     * @return
     */
    @PostMapping(value = {"/passwordReminder/{identifierOrEmail}"})
    public ResponseEntity<?> sendLinkToPasswordScreen (@PathVariable String identifierOrEmail) {
        return emailService.sendLinkToPasswordScreen(identifierOrEmail);
    }

}
