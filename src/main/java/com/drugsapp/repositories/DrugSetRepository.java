package com.drugsapp.repositories;

import com.drugsapp.entities.DrugSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DrugSetRepository extends JpaRepository<DrugSet, UUID> {
}
