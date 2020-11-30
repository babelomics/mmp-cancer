package com.fujitsu.mmp.msusermanagement.services;

import com.fujitsu.mmp.msusermanagement.dto.ConfigurationDTO;
import com.fujitsu.mmp.msusermanagement.dto.pandrugsapi.DrugResponse;
import com.fujitsu.mmp.msusermanagement.dto.pandrugsapi.LoginSession;
import com.fujitsu.mmp.msusermanagement.dto.pandrugsapi.PandrugsConfigurationDTO;
import com.fujitsu.mmp.msusermanagement.dto.pandrugsapi.Registry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class PandrugService {

    private final WebClient webClient;

    @Autowired
    public PandrugService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://www.pandrugs.org").build();
    }

    public ResponseEntity<?> validate(PandrugsConfigurationDTO pandrugsConfigurationDTO) {
        HttpStatus responseStatus = HttpStatus.OK;
        PandrugsConfigurationDTO responseBody = pandrugsConfigurationDTO;

        if(pandrugsConfigurationDTO.getBaseUrl() == null) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else{
            WebClient pandrugsClient = WebClient.create(pandrugsConfigurationDTO.getBaseUrl());
            LoginSession loginSession = new LoginSession(pandrugsConfigurationDTO.getUser(),pandrugsConfigurationDTO.getPassword());

            try {
                pandrugsClient
                        .post()
                        .uri("/api/session/")
                        .body(Mono.just(loginSession), LoginSession.class)
                        .retrieve()
                        .bodyToFlux(ResponseEntity.class)
                        .collectList()
                        .block();

            } catch ( WebClientResponseException e ) {

                return new ResponseEntity<>(e.getMessage(),e.getStatusCode());
            }

        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<?> register(PandrugsConfigurationDTO pandrugsConfigurationDTO) {
        HttpStatus responseStatus = HttpStatus.OK;
        PandrugsConfigurationDTO responseBody = pandrugsConfigurationDTO;

        if(pandrugsConfigurationDTO.getBaseUrl() == null) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else{
            WebClient pandrugsClient = WebClient.create(pandrugsConfigurationDTO.getBaseUrl());
            Registry reigstrySession = new Registry(pandrugsConfigurationDTO.getUser(),pandrugsConfigurationDTO.getPassword(),
                    pandrugsConfigurationDTO.getEmail(), UUID.randomUUID().toString());

            try {
                pandrugsClient
                        .post()
                        .uri("/api/registration/")
                        .body(Mono.just(reigstrySession), Registry.class)
                        .retrieve()
                        .bodyToFlux(String.class)
                        .collectList()
                        .block();

            } catch ( WebClientResponseException e ) {

                return new ResponseEntity<>(e.getMessage(),e.getStatusCode());
            }

        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public Boolean isValid(String pandrugURL, String pandrugUser, String pandrugPassword) {
        WebClient pandrugsClient = WebClient.create(pandrugURL);
        LoginSession loginSession = new LoginSession(pandrugUser, pandrugPassword);

        try {
            pandrugsClient
                    .post()
                    .uri("/api/session/")
                    .body(Mono.just(loginSession), LoginSession.class)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .collectList()
                    .block();

        } catch ( WebClientResponseException e ) {
            return false;
        }

        return true;
    }
}
