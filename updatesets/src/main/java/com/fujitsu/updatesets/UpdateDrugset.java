package com.fujitsu.updatesets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fujitsu.drugsapp.controllers.DrugsAPIController;
import com.fujitsu.drugsapp.entities.Drug;
import com.fujitsu.drugsapp.entities.DrugSet;
import com.fujitsu.drugsapp.services.DrugSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;


public class UpdateDrugset {

    public static void main(String[] args) {
        SpringApplication.run(UpdateDrugset.class, args);
    }

    @Autowired
    private DrugSetService drugSetService;

    public DrugSet getPandrugSet() throws JsonProcessingException {
        DrugSet drugSet = new DrugSet();
        DrugsAPIController panDrugsController = new DrugsAPIController();
        drugSet = panDrugsController.getAllDrugs();

        return drugSet;
    }

    public DrugSet AsyncUpdateDrugset(DrugSet drugSet) {

        if (!drugSetService.existByName(drugSet)) {
            drugSetService.saveDrugSet(drugSet);
        } else {
            List<Drug> drugs = drugSet.getDrugs();
            drugSet = drugSetService.findByName(drugSet.getName());
            drugSet.setDrugs(drugs);
            drugSetService.updateDrugSet(drugSet);
        }

        return drugSet;
    }
}
