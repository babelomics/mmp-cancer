package com.fujitsu.commondependencies.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fujitsu.commondependencies.entities.Drug;
import com.fujitsu.commondependencies.interfaces.DrugsAPIInterface;
import com.fujitsu.commondependencies.pandrugs.PandrugsDTO;
import com.fujitsu.commondependencies.entities.DrugName;
import com.fujitsu.commondependencies.entities.DrugSet;
import com.fujitsu.commondependencies.entities.DrugSource;
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
            drug.getDrugNames().add(drugName);

            for(int j = 0; j < pandrugsDTO.get(i).getSourceName().size(); ++j) {
                DrugSource drugSource = new DrugSource();
                drugSource.setShortName(pandrugsDTO.get(i).getSourceName().get(j).getName());
                drugName.setDrugSource(drugSource);
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

}
