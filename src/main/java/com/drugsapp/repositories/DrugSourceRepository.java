package com.drugsapp.repositories;

import com.drugsapp.entities.DrugSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DrugSourceRepository extends JpaRepository<DrugSource, UUID> {
}
