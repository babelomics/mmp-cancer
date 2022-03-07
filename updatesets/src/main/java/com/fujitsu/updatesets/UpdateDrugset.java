package com.fujitsu.updatesets;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(scanBasePackages = "com.fujitsu.drugsapp")
@Configuration
@EnableScheduling
public class UpdateDrugset {

    public static void main(String[] args) {
        SpringApplication.run(UpdateDrugset.class, args);
    }
}
