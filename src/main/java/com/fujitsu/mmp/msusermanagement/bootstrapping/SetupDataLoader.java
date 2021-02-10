package com.fujitsu.mmp.msusermanagement.bootstrapping;

import com.fujitsu.mmp.msusermanagement.entities.Configuration;
import com.fujitsu.mmp.msusermanagement.entities.User;
import com.fujitsu.mmp.msusermanagement.repositories.ConfigurationRepository;
import com.fujitsu.mmp.msusermanagement.repositories.UserRepository;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SetupDataLoader implements
        ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    UserRepository userRepository;

    @Value("${pandrugs.base.url}")
    String PANDRUGS_BASE_URL;

    @Value("${genomic.dictionary.base.url}")
    String GENOMIC_DICTIONARY_BASE_URL;

    private static final Logger log = LoggerFactory.getLogger(SetupDataLoader.class);

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    ConfigurationRepository configurationRepository;

    boolean alreadySetup = false;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (alreadySetup) {
            return;
        }

        if (mongoTemplate.getCollectionNames().isEmpty()) {
            mongoTemplate.createCollection("users");
            User adminUser = new User();
            adminUser.setIdentifier("admin");

            adminUser.setPassword(encoder.encode("admin"));
            adminUser.setEmail("noreply.mmpcancer@gmail.com");
            adminUser.setUserType("Admin");

            userRepository.save(adminUser);

            mongoTemplate.createCollection("usersHistory");
            mongoTemplate.createCollection("usersRegistryRequests");
            mongoTemplate.createCollection("permissions");
            mongoTemplate.createCollection("notifications");
            mongoTemplate.createCollection("drugHistory");

            mongoTemplate.createCollection("configurations");
            Configuration configuration = new Configuration();
            configuration.setPandrugURL(PANDRUGS_BASE_URL);
            configuration.setGenomicDictionaryURL(GENOMIC_DICTIONARY_BASE_URL);
            configurationRepository.save(configuration);
        }

        alreadySetup = true;
    }


}