package com.fujitsu.commondependencies.interfaces;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fujitsu.commondependencies.entities.DrugSet;

public interface DrugsAPIInterface {
    DrugSet getAllDrugs() throws JsonProcessingException;
}
