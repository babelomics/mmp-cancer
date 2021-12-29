package com.fujitsu.drugsapp.interfaces;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fujitsu.drugsapp.entities.Drug;
import com.fujitsu.drugsapp.entities.DrugSet;

import java.util.List;

public interface DrugsAPIInterface {
    DrugSet getAllDrugs() throws JsonProcessingException;
    DrugSet retrieveDrugsFromGenes(List<String> genes);
    List<String> retrieveGenesFromVariants(List<String> variants);
}
