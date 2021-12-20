package com.fujitsu.drugsapp.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fujitsu.drugsapp.entities.Drug;
import com.fujitsu.drugsapp.repositories.DrugRepository;
import com.fujitsu.drugsapp.repositories.DrugSetRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@AllArgsConstructor
@Service
public class DrugService {

    private final DrugRepository drugRepository;

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

    public boolean existByStandardName(Drug drug){
        List<Drug> findDrug = drugRepository.findAll();

        for(int i=0; i<findDrug.size(); ++i){
                if(drug.getStandardName().toLowerCase().equals(findDrug.get(i).getStandardName().toLowerCase()))
                    return true;
        }

        return false;
    }
}
