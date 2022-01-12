package com.fujitsu.drugsapp.services;

import com.fujitsu.drugsapp.entities.*;
import com.fujitsu.drugsapp.repositories.DrugRepository;
import com.fujitsu.drugsapp.repositories.DrugSetRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@AllArgsConstructor
@Service
public class DrugSetService {

    private final DrugSetRepository drugSetRepository;
    private final DrugRepository drugRepository;
    private final DrugService drugService;
    private final DrugUpdateService drugUpdateService;
    private final DrugNameService drugNameService;
    private final DrugSourceService drugSourceService;

    public List<DrugSet> findAll(String searchText){

        if(searchText==null) {
            return drugSetRepository.findAll();
        }else{
            List<DrugSet> drugSets = drugSetRepository.findAll();
            List<DrugSet> matchedDrugSets = new ArrayList<>();

            for (DrugSet drugSet : drugSets) {
                if (drugSet.getName().toLowerCase().contains(searchText.toLowerCase())
                        || drugSet.getDescription().toLowerCase().contains(searchText.toLowerCase()))
                    matchedDrugSets.add(drugSet);
            }
            return matchedDrugSets;
        }
    }

    public DrugSet findById(UUID uuid) {
        return drugSetRepository.findById(uuid).orElseThrow(NoSuchElementException::new);
    }

    public List<Drug> findDrugsById(UUID uuid, String searchText, Instant date) {
        DrugSet drugSet = drugSetRepository.findById(uuid).orElseThrow(NoSuchElementException::new);
        List<Drug> matchedDrugs = new ArrayList<>();
        List<Drug> drugs = drugRepository.findAll();

        if(searchText==null && date==null) {
            matchedDrugs = drugSet.getDrugs();
        }else if(searchText!=null && date==null){

            for (Drug drug : drugs) {
                if (drug.getStandardName().toLowerCase().contains(searchText.toLowerCase())
                        || drug.getCommonName().toLowerCase().contains(searchText.toLowerCase()))
                    matchedDrugs.add(drug);
            }
        }else if(searchText==null && date!=null){
            LocalDateTime localDateTime = LocalDateTime.ofInstant(date, ZoneOffset.UTC);
            for (Drug drug : drugs) {
                if (drugSet.getUpdatedAt().isEqual(localDateTime))
                    matchedDrugs.add(drug);
            }
        }else {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(date, ZoneOffset.UTC);
            List<Drug> matchedDrugsAux = new ArrayList<>();

            for (Drug drug : drugs) {
                if (drugSet.getUpdatedAt().isEqual(localDateTime))
                    matchedDrugsAux.add(drug);
            }

            for(int i=0; i<matchedDrugsAux.size(); ++i){
                if(matchedDrugsAux.get(i).getStandardName().toLowerCase().contains(searchText.toLowerCase())
                        || matchedDrugsAux.get(i).getCommonName().toLowerCase().contains(searchText.toLowerCase()))
                    matchedDrugs.add(drugs.get(i));
            }
        }

        return matchedDrugs;
    }

    public DrugSet findByName(String name){

        List<DrugSet> drugSetList = drugSetRepository.findAll();

        for(DrugSet drugSet : drugSetList){
            if(name.equals(drugSet.getName())){
                return drugSet;
            }
        }

        return null;
    }

    public DrugSet saveDrugSet(DrugSet drugSet){
        List<Drug> newDrugs = drugSet.getDrugs();

        DrugUpdate drugUpdate = registerUpdate(drugSet.getId());
        drugUpdateService.saveDrugUpdate(drugUpdate);

        drugSet.setDrugs(null);
        drugSetRepository.save(drugSet);

        registerDrugsToDrugSet(drugSet, newDrugs, drugUpdate);

        return drugSet;
    }

    public void deleteDrugSet(UUID uuid){
        drugSetRepository.deleteById(uuid);
    }

    public DrugUpdate registerUpdate(UUID drugSetId){
        DrugUpdate drugUpdate = new DrugUpdate();
        drugUpdate.setDrugSetId(drugSetId);

        return drugUpdate;
    }

    public void registerDrugsToDrugSet(DrugSet drugSet, List<Drug> newDrugs, DrugUpdate drugUpdate){

        boolean exists = false;
        int batchSize = 100;
        for(int i = 0; i<newDrugs.size(); i += batchSize) {
            List<Drug> batchDrugs = new ArrayList<>();
            List<DrugName> batchNames = new ArrayList<>();

            for(int j = i; j<i+ batchSize && j<newDrugs.size() ; ++j) {

                exists = drugService.existByStandardName(newDrugs.get(j));

                if (!exists) {

                    List<DrugName> drugNamesList = newDrugs.get(j).getDrugNames();
                    newDrugs.get(j).setDrugNames(null);
                    batchDrugs.add(newDrugs.get(j));


                    for (DrugName drugName : drugNamesList) {
                        drugName.setDrug(newDrugs.get(j));
                        batchNames.add(drugName);

                        if(!drugSourceService.existByShortName(drugName.getDrugSource())) {
                            drugSourceService.saveDrugSource(drugName.getDrugSource());
                        }else{
                            DrugSource drugSource = drugSourceService.findByShortName(drugName.getDrugSource().getShortName());
                            drugName.setDrugSource(drugSource);
                        }
                    }

                    newDrugs.get(j).setStartUpdate(drugUpdate.getId());
                    newDrugs.get(j).setDrugSet(drugSet);
                }
            }

            drugService.saveAll(batchDrugs);
            drugNameService.saveAll(batchNames);

        }
    }

    public List<DrugUpdate> getDrugSetUpdates(UUID drugSetId){
        return drugUpdateService.findByDrugSetId(drugSetId);
    }

    public DrugSet updateDrugSet(DrugSet drugSet){

        List<Drug> newDrugs = drugSet.getDrugs();
        drugSet.setDrugs(null);

        DrugUpdate drugUpdate = new DrugUpdate();
        drugUpdate.setDrugSetId(drugSet.getId());
        List<DrugUpdate> drugUpdateList = drugUpdateService.findAll();
        drugUpdateList.get(drugUpdateList.size()-1).setNextUpdateId(drugUpdate.getId());
        drugUpdate.setPreviousUpdateId(drugUpdateList.get(drugUpdateList.size()-1).getId());
        drugUpdateService.saveDrugUpdate(drugUpdate);
        drugUpdateService.updateDrugUpdate(drugUpdateList.get(drugUpdateList.size()-1));
        drugSet.setUpdatedAt(drugUpdate.getCreatedAt());

        drugSetRepository.save(drugSet);

        registerDrugsToDrugSet(drugSet, newDrugs, drugUpdate);

        return drugSet;
    }

    public boolean existById(UUID uuid){
        return drugSetRepository.existsById(uuid);
    }

    public boolean existByName(DrugSet drugSet){
        List<DrugSet> findDrugSet = drugSetRepository.findAll();

        for (DrugSet set : findDrugSet) {
            if (drugSet.getName().toLowerCase().equals(set.getName().toLowerCase()))
                return true;
        }

        return false;
    }

}
