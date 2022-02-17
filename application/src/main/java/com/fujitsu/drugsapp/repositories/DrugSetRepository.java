package com.fujitsu.drugsapp.repositories;

import com.fujitsu.drugsapp.entities.DrugSet;
import com.fujitsu.drugsapp.entities.Drug;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DrugSetRepository extends JpaRepository<DrugSet, UUID> {

    List<DrugSet> findAll();

    List<Drug> findDrugsById(UUID uuid);

}
