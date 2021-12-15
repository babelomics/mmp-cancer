package com.fujitsu.drugsapp.services;

import com.fujitsu.drugsapp.entities.Drug;
import com.fujitsu.drugsapp.entities.DrugSet;
import com.fujitsu.drugsapp.repositories.DrugRepository;
import com.fujitsu.drugsapp.repositories.DrugSetRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@AllArgsConstructor
@Service
public class DrugSetService {

    private final DrugSetRepository drugSetRepository;
    private final DrugRepository drugRepository;
    private final DrugService drugService;

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

    public List<Drug> findDrugsById(UUID uuid, String searchText, LocalDateTime date) {
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

            for(int i=0; i<drugs.size(); ++i){
                if(drugSet.getUpdateAt().isEqual(date))
                            matchedDrugs.add(drugs.get(i));
            }
        }else {

            List<Drug> matchedDrugsAux = new ArrayList<>();

            for(int i=0; i<drugs.size(); ++i){
                if(drugSet.getUpdateAt().isEqual(date))
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
        UUID uuid = UUID.randomUUID();
        drugSet.setId(uuid);

        for(int i=0; i<newDrugs.size(); ++i) {
            exists = drugService.existByStandardName(newDrugs.get(i));
            if(!exists){
                drugService.saveDrug(newDrugs.get(i));
            }else{
                drugSet.getDrugs().remove(i);
            }
        }

        return drugSetRepository.save(drugSet);
    }

    public void deleteDrugSet(UUID uuid){
        drugSetRepository.deleteById(uuid);
    }

    public DrugSet updateDrugSet(DrugSet drugSet){
        return drugSetRepository.save(drugSet);
    }

    public boolean existById(UUID uuid){
        return drugSetRepository.existsById(uuid);
    }
}
