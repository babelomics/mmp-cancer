package com.fujitsu.drugsapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@ComponentScan(basePackages = {"com.fujitsu.commondependencies","com.fujitsu.drugsapp","com.fujitsu.updatesets"})
@EntityScan("com.fujitsu.commondependencies")
@EnableJpaRepositories("com.fujitsu.commondependencies")
@SpringBootApplication(scanBasePackages = "com.fujitsu.commondependencies")
public class DrugsAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(DrugsAppApplication.class, args);
	}

}
