package com.fujitsu.drugsapp.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fujitsu.drugsapp.dto.pandrugs.PandrugsDTO;
import com.fujitsu.drugsapp.entities.Drug;
import com.fujitsu.drugsapp.entities.DrugName;
import com.fujitsu.drugsapp.entities.DrugSet;
import com.fujitsu.drugsapp.entities.DrugSource;
import com.fujitsu.drugsapp.interfaces.DrugsAPIInterface;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class DrugsAPIController implements DrugsAPIInterface {

    private final String PANDRUGS_BASE_URL="https://www.pandrugs.org/pandrugs-backend/api/genedrug/";

    @Override
    public DrugSet getAllDrugs() throws JsonProcessingException {

        String query = PANDRUGS_BASE_URL + "drug";
        RestTemplate restTemplate = null;
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        restTemplate = restTemplateBuilder.build();
        String response = restTemplate.getForObject(query, String.class);
        ObjectMapper objectMapper = new ObjectMapper();

        List<PandrugsDTO> pandrugsDTO = objectMapper.readValue(response, new TypeReference<List<PandrugsDTO>>(){});
        List<Drug> drugs = new ArrayList<>();
        DrugSet drugSet = new DrugSet();

        for(int i = 0; i < pandrugsDTO.size(); ++i){
            Drug drug = new Drug();
            DrugName drugName = new DrugName();

            drug.setStandardName(pandrugsDTO.get(i).getStandardName());
            drug.setCommonName(pandrugsDTO.get(i).getShowName());

            drugName.setName(pandrugsDTO.get(i).getStandardName());
            drugName.setDrug(drug);
            drugName.setDrugId(drug.getUuid());
            drug.getDrugNames().add(drugName);

            for(int j = 0; j < pandrugsDTO.get(i).getSourceName().size(); ++j) {
                DrugSource drugSource = new DrugSource();
                drugSource.setName(pandrugsDTO.get(i).getSourceName().get(j).getName());
                drugSource.setShortName(pandrugsDTO.get(i).getSourceName().get(j).getDrugName());
                drug.getDrugSources().add(drugSource);
            }

            drugs.add(drug);
        }

        String description = "DrugSet from Pandrugs";
        String name = "Pandrugs DrugSet";

        drugSet.setName(name);
        drugSet.setDescription(description);
        drugSet.setDrugs(drugs);

        return drugSet;
    }

    @Override
    public DrugSet retrieveDrugsFromGenes(List<String> genes) {

        DrugSet drugSet = null;
        String query = PANDRUGS_BASE_URL + "?" + "gene=" + genes.get(0) + "&biomarker=true";
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
