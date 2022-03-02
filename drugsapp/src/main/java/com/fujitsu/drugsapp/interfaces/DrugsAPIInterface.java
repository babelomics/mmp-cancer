package com.fujitsu.drugsapp.interfaces;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fujitsu.drugsapp.entities.DrugSet;

public interface DrugsAPIInterface {
    DrugSet getAllDrugs() throws JsonProcessingException;
}
