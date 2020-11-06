package com.fujitsu.mmp.msusermanagement.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfiguration {

    @Value("${mail.host}")
    private String mailServerHost;

    @Value("${mail.port}")
    private Integer mailServerPort;

    @Value("${mail.username}")
    private String mailServerUsername;

    @Value("${mail.password}")
    private String mailServerPassword;

    @Value("${mail.properties.mail.smtp.auth}")
    private String mailServerAuth;

    @Value("${mail.properties.mail.smtp.starttls.enable}")
    private String mailServerStartTls;


    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(mailServerHost);
        mailSender.setPort(mailServerPort);

        mailSender.setUsername(mailServerUsername);
        mailSender.setPassword(mailServerPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", mailServerAuth);
        props.put("mail.smtp.starttls.enable", mailServerStartTls);
        props.put("mail.debug", "true");

        return mailSender;
    }
}
