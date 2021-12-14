package com.drugsapp.repositories;

import com.drugsapp.entities.DrugName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DrugNameRepository extends JpaRepository<DrugName, UUID> {
}
