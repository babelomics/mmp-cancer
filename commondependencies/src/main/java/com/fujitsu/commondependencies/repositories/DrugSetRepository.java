package com.fujitsu.commondependencies.repositories;

import com.fujitsu.commondependencies.entities.Drug;
import com.fujitsu.commondependencies.entities.DrugSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DrugSetRepository extends JpaRepository<DrugSet, UUID> {

    List<DrugSet> findAll();

    List<Drug> findDrugsById(UUID uuid);

}
