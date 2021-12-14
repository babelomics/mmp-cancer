package com.drugsapp.services;

import com.drugsapp.entities.Drug;
import com.drugsapp.repositories.DrugRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@AllArgsConstructor
@Service
public class DrugService {

    private DrugRepository drugRepository;

    public List<Drug> findAll(){ return drugRepository.findAll(); }

    public Drug findById(UUID id){
        return drugRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    public Drug saveDrug(Drug drug){ return drugRepository.save(drug); }

    public void deleteDrug(UUID id){ drugRepository.deleteById(id); }

    public Drug updateDrug(Drug drug){
        return drugRepository.save(drug);
    }

    public boolean existById(UUID uuid){
        return drugRepository.existsById(uuid);
    }
}
