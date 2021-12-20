package com.fujitsu.drugsapp.controllers;

import com.fujitsu.drugsapp.entities.Drug;
import com.fujitsu.drugsapp.entities.DrugSet;
import com.fujitsu.drugsapp.interfaces.DrugsAPIInterface;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@Component
public class DrugsAPIController implements DrugsAPIInterface {

    private final String PANDRUGS_BASE_URL="https://www.pandrugs.org/pandrugs-backend/api/genedrug/";

    @Override
    public List<Drug> getAllDrugs() {

        String query = PANDRUGS_BASE_URL + "drug";
        RestTemplate restTemplate = null;
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        restTemplate = restTemplateBuilder.build();
        String response = restTemplate.getForObject(query, String.class);

        return null;
    }

    @Override
    public DrugSet retrieveDrugsFromGenes(List<String> genes) {

        DrugSet drugSet = null;
        String query = PANDRUGS_BASE_URL + "?gene=" + genes.get(0) + "&biomarker=true";
        RestTemplate restTemplate = null;
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        restTemplate = restTemplateBuilder.build();
        String response = restTemplate.getForObject(query, String.class);

        return drugSet;
    }

    @Override
    public List<String> retrieveGenesFromVariants(List<String> variants) {

        return variants;
    }
}
