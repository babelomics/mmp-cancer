package com.fujitsu.mmp.msusermanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class MsUserManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsUserManagementApplication.class, args);
	}
}
