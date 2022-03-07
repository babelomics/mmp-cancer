package com.fujitsu.updatesets;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.fujitsu.commondependencies", "com.fujitsu.updatesets"})
@EntityScan("com.fujitsu.commondependencies")
@EnableJpaRepositories("com.fujitsu.commondependencies")
public class UpdateSetsMain {

    public static void main(String[] args) {
        SpringApplication.run(com.fujitsu.updatesets.UpdateSetsMain.class, args);
    }
}

