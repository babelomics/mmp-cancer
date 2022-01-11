package com.fujitsu.drugsapp.services;

import com.fujitsu.drugsapp.entities.Drug;
import com.fujitsu.drugsapp.entities.DrugName;
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

    public DrugSource findByShortName(String shortName){

        List<DrugSource> drugSourceList = findAll();

        for(DrugSource drugSource : drugSourceList){
            if(shortName.equals(drugSource.getShortName())){
                return drugSource;
            }
        }

        return null;
    }

    public DrugSource saveDrugSource(DrugSource drugSource){ return drugSourceRepository.save(drugSource); }

    public List<DrugSource> saveAll(List<DrugSource> drugSourceList){ return drugSourceRepository.saveAll(drugSourceList); }

    public void deleteDrugSource(UUID id){ drugSourceRepository.deleteById(id); }

    public DrugSource updateDrugSource(DrugSource drugSource){
        return drugSourceRepository.save(drugSource);
    }

    public boolean existById(UUID uuid){
        return drugSourceRepository.existsById(uuid);
    }

    public boolean existByShortName(DrugSource drugSource){
        List<DrugSource> findDrugSource = drugSourceRepository.findAll();

        for(int i=0; i<findDrugSource.size(); ++i){
            if(drugSource.getShortName().toLowerCase().equals(findDrugSource.get(i).getShortName().toLowerCase()))
                return true;
        }

        return false;
    }
}
