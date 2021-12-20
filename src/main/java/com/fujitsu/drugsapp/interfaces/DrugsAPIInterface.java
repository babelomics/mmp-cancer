package com.fujitsu.drugsapp.interfaces;

import com.fujitsu.drugsapp.entities.Drug;
import com.fujitsu.drugsapp.entities.DrugSet;

import java.util.List;

public interface DrugsAPIInterface {
    List<Drug> getAllDrugs();
    DrugSet retrieveDrugsFromGenes(List<String> genes);
    List<String> retrieveGenesFromVariants(List<String> variants);
}
