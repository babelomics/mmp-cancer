package com.fujitsu.drugsapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.fujitsu")
@SpringBootApplication
public class DrugsAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(DrugsAppApplication.class, args);
	}

}
