package com.fujitsu.mmp.msusermanagement.bootstrapping;

import com.fujitsu.mmp.msusermanagement.constants.UserConstants;
import com.fujitsu.mmp.msusermanagement.entities.Configuration;
import com.fujitsu.mmp.msusermanagement.entities.User;
import com.fujitsu.mmp.msusermanagement.repositories.ConfigurationRepository;
import com.fujitsu.mmp.msusermanagement.repositories.UserRepository;
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
            adminUser.setFirstName("Admin");
            adminUser.setLastName("Admin");
            adminUser.setOrganization("Admin Organization");
            adminUser.setUserType(UserConstants.USER_TYPE_ADMIN);

            userRepository.save(adminUser);

            mongoTemplate.createCollection("configurations");

            Configuration configuration = new Configuration();

            configuration.setPandrugURL(PANDRUGS_BASE_URL);
            configuration.setGenomicDictionaryURL(GENOMIC_DICTIONARY_BASE_URL);
            configuration.setContactIdentifier("admin");

            configurationRepository.save(configuration);
        }

        alreadySetup = true;
    }


}