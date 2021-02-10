package com.fujitsu.mmp.msusermanagement.services;

import com.fujitsu.mmp.msusermanagement.dto.configuration.ConfigurationDTO;
import com.fujitsu.mmp.msusermanagement.entities.Configuration;
import com.fujitsu.mmp.msusermanagement.mappers.ConfigurationMapper;
import com.fujitsu.mmp.msusermanagement.repositories.ConfigurationRepository;
import com.fujitsu.mmp.msusermanagement.utility.JWTUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class ConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

    @Autowired
    ConfigurationRepository configurationRepository;

    @Autowired
    ConfigurationMapper configurationMapper;

    @Autowired
    GenomicDictionaryService genomicDictionaryService;

    @Autowired
    PandrugService pandrugService;

    @Autowired
    JWTUtility jwtUtility;

    public ResponseEntity<?> getConfiguration(HttpServletRequest httpServletRequest) throws IOException {
        String authorization = httpServletRequest.getHeader("Authorization");
        ConfigurationDTO responseBody = new ConfigurationDTO();
        HttpStatus responseStatus = HttpStatus.OK;
        List<Configuration> configurationList = configurationRepository.findAll();
        Configuration configuration = null;

        if(!configurationList.isEmpty()){
            configuration = configurationList.get(0);
        }
        
        if(authorization == null){
            if(isConfiguratedOK()){
                responseBody.setSetupInformation(configuration.getSetupInformation());
                responseBody.setContactEmail(configuration.getContactEmail());
            }else{
                return new ResponseEntity<>(
                        "Error: The application is not configured yet.",
                        HttpStatus.NOT_FOUND);
            }
        }else{
            responseBody = configurationMapper.entityToDto(configuration);
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }


    public ResponseEntity<?> updateConfiguration(ConfigurationDTO configurationDTO, HttpServletRequest httpServletRequest) throws IOException {
        HttpStatus responseStatus = HttpStatus.OK;
        Boolean isConfigured = false;

        String token = httpServletRequest.getHeader("Authorization");
        String username = "";

        if(token != null) {
            username = jwtUtility.getUsernameFromToken(token.substring(6));
        }

        List<Configuration> entityList = configurationRepository.findAll();
        Configuration entity = entityList.get(0);
        if (entityList.isEmpty()) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            Configuration entityToSave = configurationMapper.dtoToEntity(configurationDTO);
            entityToSave.setId(entity.getId());
            entityToSave = configurationRepository.save(entityToSave);
            if (isConfiguratedOK()) {
                isConfigured = true;
            }
        }

        logger.info("Configuration updated by: "+username+" on "+ new Date());

        return new ResponseEntity<>(isConfigured, responseStatus);
    }

    public ResponseEntity<ConfigurationDTO> updateContactAdmin(ConfigurationDTO configurationDTO, HttpServletRequest httpServletRequest) {
        HttpStatus responseStatus = HttpStatus.OK;
        ConfigurationDTO responseBody = null;

        String token = httpServletRequest.getHeader("Authorization");
        String username = "";

        if(token != null) {
            username = jwtUtility.getUsernameFromToken(token.substring(6));
        }

        List<Configuration> entityList = configurationRepository.findAll();
        Configuration entity = entityList.get(0);
        if (entityList.isEmpty()) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            Configuration entityToSave = configurationMapper.dtoToEntity(configurationDTO);
            entityToSave.setPandrugPassword(entity.getPandrugPassword());
            entityToSave.setPandrugURL(entity.getPandrugURL());
            entityToSave.setPandrugUser(entity.getPandrugUser());
            entityToSave.setGenomicDictionaryURL(entity.getGenomicDictionaryURL());
            entityToSave.setSetupInformation(entity.getSetupInformation());
            entityToSave.setId(entity.getId());
            entityToSave = configurationRepository.save(entityToSave);
            responseBody = configurationMapper.entityToDto(entityToSave);
        }

        logger.info("Contact admin updated by: "+username+" on "+ new Date());

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    private Boolean isConfiguratedOK() throws IOException {
        List<Configuration> configurationList = configurationRepository.findAll();
        Configuration configuration = null;
        if(!configurationList.isEmpty()) {
            configuration = configurationList.get(0);
        }

        if(configuration == null) {
            return false;
        } else {
            if(!genomicDictionaryService.isValid(configuration.getGenomicDictionaryURL())){
                return false;
            }else{
                if(!pandrugService.isValid(configuration.getPandrugURL(), configuration.getPandrugUser(),
                        configuration.getPandrugPassword())){
                    return false;
                }
            }
        }
        return true;
    }
}
