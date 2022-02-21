package com.fujitsu.drugsapp;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.fujitsu"})
@EnableJpaRepositories("com.fujitsu")
@EntityScan("com.fujitsu")
@EnableBatchProcessing
@EnableScheduling
public class DrugsAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(DrugsAppApplication.class, args);
	}

}
