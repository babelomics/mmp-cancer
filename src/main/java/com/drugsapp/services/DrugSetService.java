package com.drugsapp.services;

import com.drugsapp.entities.DrugSet;
import com.drugsapp.repositories.DrugSetRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@AllArgsConstructor
@Service
public class DrugSetService {

    private final DrugSetRepository drugSetRepository;

    public List<DrugSet> findAll(){
        return drugSetRepository.findAll();
    }

    public DrugSet findById(UUID uuid, String searchText) {

        if(searchText.equals("") || searchText==null) {
            return drugSetRepository.findById(uuid).orElseThrow(NoSuchElementException::new);
        }else{
            return null;
        }
    }

    public DrugSet saveDrugSet(DrugSet drugSet){
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
