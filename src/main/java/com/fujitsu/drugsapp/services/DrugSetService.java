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

            for(int i=0; i<drugSets.size(); ++i){
                if(drugSets.get(i).getName().toLowerCase().contains(searchText.toLowerCase())
                        || drugSets.get(i).getDescription().toLowerCase().contains(searchText.toLowerCase()))
                    matchedDrugSets.add(drugSets.get(i));
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

            for(int i=0; i<drugs.size(); ++i){
                if(drugs.get(i).getStandardName().toLowerCase().contains(searchText.toLowerCase())
                        || drugs.get(i).getCommonName().toLowerCase().contains(searchText.toLowerCase()))
                    matchedDrugs.add(drugs.get(i));
            }
        }else if(searchText==null && date!=null){
            LocalDateTime localDateTime = LocalDateTime.ofInstant(date, ZoneOffset.UTC);
            for(int i=0; i<drugs.size(); ++i){
                if(drugSet.getUpdateAt().isEqual(localDateTime))
                            matchedDrugs.add(drugs.get(i));
            }
        }else {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(date, ZoneOffset.UTC);
            List<Drug> matchedDrugsAux = new ArrayList<>();

            for(int i=0; i<drugs.size(); ++i){
                if(drugSet.getUpdateAt().isEqual(localDateTime))
                    matchedDrugsAux.add(drugs.get(i));
            }

            for(int i=0; i<matchedDrugsAux.size(); ++i){
                if(matchedDrugsAux.get(i).getStandardName().toLowerCase().contains(searchText.toLowerCase())
                        || matchedDrugsAux.get(i).getCommonName().toLowerCase().contains(searchText.toLowerCase()))
                    matchedDrugs.add(drugs.get(i));
            }
        }

        return matchedDrugs;
    }

    public DrugSet saveDrugSet(DrugSet drugSet){
        List<Drug> newDrugs = drugSet.getDrugs();
        boolean exists = false;

        for(int i=0; i<newDrugs.size(); ++i) {
            exists = drugService.existByStandardName(newDrugs.get(i));

            if(!exists){

                List<DrugName> drugNamesList = newDrugs.get(i).getDrugNames();
                newDrugs.get(i).setDrugNames(null); //Solucion provisional

                drugSourceService.saveDrugSourceList(newDrugs.get(i).getDrugSources());
                drugService.saveDrug(newDrugs.get(i));

                drugNameService.saveDrugName(drugNamesList.get(0));
            }else{
                drugSet.getDrugs().set(i,null);
            }
        }

        drugSet.getDrugs().removeAll(Collections.singleton(null));

        DrugUpdate drugUpdate = registerUpdate(drugSet.getId());
        drugUpdateService.saveDrugUpdate(drugUpdate);

        return drugSetRepository.save(drugSet);
    }

    public void deleteDrugSet(UUID uuid){
        drugSetRepository.deleteById(uuid);
    }

    public DrugUpdate registerUpdate(UUID drugSetId){
        DrugUpdate drugUpdate = new DrugUpdate();
        drugUpdate.setDrugSetId(drugSetId);

        return drugUpdate;
    }

    public List<DrugUpdate> getDrugSetUpdates(UUID drugSetId){
        return drugUpdateService.findByDrugSetId(drugSetId);
    }

    public DrugUpdate updateDrugSet(DrugSet drugSet){

        DrugUpdate drugUpdate = new DrugUpdate();
        drugUpdate.setDrugSetId(drugSet.getId());
        List<DrugUpdate> drugUpdateList = drugUpdateService.findAll();
        drugUpdateList.get(drugUpdateList.size()-1).setNextUpdateId(drugUpdate.getId());
        drugUpdate.setPreviousUpdateId(drugUpdateList.get(drugUpdateList.size()-1).getId());
        drugUpdateService.updateDrugUpdate(drugUpdateList.get(drugUpdateList.size()-1));
        drugUpdateService.saveDrugUpdate(drugUpdate);

        return drugUpdate;
    }

    public boolean existById(UUID uuid){
        return drugSetRepository.existsById(uuid);
    }
}
