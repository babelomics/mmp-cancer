package com.fujitsu.mmp.msusermanagement.controllers;

import com.fujitsu.mmp.msusermanagement.dto.configuration.ConfigurationDTO;
import com.fujitsu.mmp.msusermanagement.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@CrossOrigin(origins = "*", maxAge = 3600, exposedHeaders = "Authorization")
@RequestMapping("/api/configuration")
@RestController
public class ConfigurationController {

    @Autowired
    ConfigurationService configurationService;

    /**
     * Get the configuration in case it is valid.
     * @return Without a valid token return the contact admin user and the installation text. With a valid token
     * get the whole configuration.
     */
    @GetMapping
    public ResponseEntity<?> getConfiguration (HttpServletRequest httpServletRequest) throws IOException {
        return configurationService.getConfiguration(httpServletRequest);
    }

    /**
     * Update the configuration.
     * @param configurationDTO: configuration object to be updated.
     * @return configuration object updated.
     */
    @PutMapping
    public ResponseEntity<?> updateConfiguration (@RequestBody ConfigurationDTO configurationDTO, HttpServletRequest httpServletRequest) throws IOException {
        return configurationService.updateConfiguration(configurationDTO, httpServletRequest);
    }

    /**
     * Update the administrator contact configuration of the system.
     * @param configurationDTO: Object with the contact identifier, name, last name and email.
     * @return administrator contact configuration object updated.
     */
    @PutMapping("/update-contact-admin")
    ResponseEntity<ConfigurationDTO> updateContactAdmin (@RequestBody ConfigurationDTO configurationDTO, HttpServletRequest httpServletRequest) {
        return configurationService.updateContactAdmin(configurationDTO, httpServletRequest);
    }
}
