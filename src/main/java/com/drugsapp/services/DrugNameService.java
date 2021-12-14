package com.drugsapp.services;

import com.drugsapp.entities.DrugName;
import com.drugsapp.repositories.DrugNameRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@AllArgsConstructor
@Service
public class DrugNameService {

    private DrugNameRepository drugNameRepository;

    public List<DrugName> findAll(){ return drugNameRepository.findAll(); }

    public DrugName findById(UUID id){
        return drugNameRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    public DrugName saveDrugName(DrugName drugName){ return drugNameRepository.save(drugName); }

    public void deleteDrugName(UUID id){ drugNameRepository.deleteById(id); }

    public DrugName updateDrugName(DrugName drugName){
        return drugNameRepository.save(drugName);
    }

    public boolean existById(UUID uuid){
        return drugNameRepository.existsById(uuid);
    }

}
