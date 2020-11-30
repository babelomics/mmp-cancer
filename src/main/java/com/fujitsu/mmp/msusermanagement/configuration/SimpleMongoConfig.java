package com.fujitsu.mmp.msusermanagement.configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class SimpleMongoConfig {

    @Value("${database.spring.data.mongodb.uri}")
    String MONGO_DB_URI;

    @Value("${database.spring.data.mongodb.database}")
    String MONGO_DB_NAME;

    private static final Logger logger = LoggerFactory.getLogger(SimpleMongoConfig.class);

    @Bean
    public MongoClient mongo() {
        ConnectionString connectionString = new ConnectionString(MONGO_DB_URI);
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();

        return MongoClients.create(mongoClientSettings);
    }

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(mongo(), MONGO_DB_NAME);
    }

}