package com.fujitsu.commondependencies.services;

import com.fujitsu.commondependencies.entities.DrugUpdate;
import com.fujitsu.commondependencies.repositories.DrugUpdateRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class DrugUpdateService {

    private DrugUpdateRepository drugUpdateRepository;

    public List<DrugUpdate> findAll(){ return drugUpdateRepository.findAll(); }

    public List<DrugUpdate> findByDrugSetId(UUID drugSetId) {
        List<DrugUpdate> drugUpdateList = findAll();
        List<DrugUpdate> machedUpdates = new ArrayList<>();

        for (DrugUpdate drugUpdate : drugUpdateList) {
            if (drugSetId.toString().equals(drugUpdate.getDrugSetId().toString())) {
                machedUpdates.add(drugUpdate);
            }
        }

        return machedUpdates;
    }

}