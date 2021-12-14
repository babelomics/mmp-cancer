package com.drugsapp.services;

import com.drugsapp.entities.DrugUpdate;
import com.drugsapp.repositories.DrugUpdateRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@AllArgsConstructor
@Service
public class DrugUpdateService {

    private DrugUpdateRepository drugUpdateRepository;

    public List<DrugUpdate> findAll(){ return drugUpdateRepository.findAll(); }

    public DrugUpdate findById(UUID id){
        return drugUpdateRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    public DrugUpdate saveDrugUpdate(DrugUpdate drugUpdate){ return drugUpdateRepository.save(drugUpdate); }

    public void deleteDrugUpdate(UUID id){ drugUpdateRepository.deleteById(id); }

    public DrugUpdate updateDrugUpdate(DrugUpdate drugUpdate){
        return drugUpdateRepository.save(drugUpdate);
    }

    public boolean existById(UUID uuid){
        return drugUpdateRepository.existsById(uuid);
    }
}
