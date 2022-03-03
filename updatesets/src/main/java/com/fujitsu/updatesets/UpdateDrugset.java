package com.fujitsu.updatesets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fujitsu.drugsapp.controllers.DrugsAPIController;
import com.fujitsu.drugsapp.entities.Drug;
import com.fujitsu.drugsapp.entities.DrugSet;
import com.fujitsu.drugsapp.services.DrugSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.util.List;

@SpringBootApplication(scanBasePackages = "com.fujitsu.drugsapp")
@Configuration
@EnableScheduling
public class UpdateDrugset {

    public static void main(String[] args) {
        SpringApplication.run(UpdateDrugset.class, args);
    }
}
