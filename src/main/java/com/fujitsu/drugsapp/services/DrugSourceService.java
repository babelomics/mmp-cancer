package com.fujitsu.drugsapp.services;

import com.fujitsu.drugsapp.entities.DrugSource;
import com.fujitsu.drugsapp.repositories.DrugSourceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@AllArgsConstructor
@Service
public class DrugSourceService {

    private DrugSourceRepository drugSourceRepository;

    public List<DrugSource> findAll(){ return drugSourceRepository.findAll(); }

    public DrugSource findById(UUID id){
        return drugSourceRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    public DrugSource saveDrugSource(DrugSource drugSource){ return drugSourceRepository.save(drugSource); }

    public void deleteDrugSource(UUID id){ drugSourceRepository.deleteById(id); }

    public DrugSource updateDrugSource(DrugSource drugSource){
        return drugSourceRepository.save(drugSource);
    }

    public boolean existById(UUID uuid){
        return drugSourceRepository.existsById(uuid);
    }
}
