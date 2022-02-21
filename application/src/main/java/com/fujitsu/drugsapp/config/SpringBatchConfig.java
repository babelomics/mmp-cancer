package com.fujitsu.drugsapp.config;

import com.fujitsu.drugsapp.entities.DrugSet;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.context.annotation.Bean;

public class SpringBatchConfig {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;


}
