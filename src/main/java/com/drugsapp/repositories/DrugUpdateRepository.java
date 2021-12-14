package com.drugsapp.repositories;

import com.drugsapp.entities.DrugUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DrugUpdateRepository extends JpaRepository<DrugUpdate, UUID> {
}
