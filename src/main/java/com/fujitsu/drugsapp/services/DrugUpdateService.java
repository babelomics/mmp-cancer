package com.fujitsu.drugsapp.services;

import com.fujitsu.drugsapp.entities.DrugUpdate;
import com.fujitsu.drugsapp.repositories.DrugUpdateRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public List<DrugUpdate> findByDrugSetId(UUID drugSetId) {
        List<DrugUpdate> drugUpdateList = findAll();
        List<DrugUpdate> machedUpdates = new ArrayList<>();

        for(int i=0; i<drugUpdateList.size(); ++i){
            if(drugSetId.toString().equals(drugUpdateList.get(i).getDrugSetId().toString())){
                machedUpdates.add(drugUpdateList.get(i));
            }
        }

        return machedUpdates;
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