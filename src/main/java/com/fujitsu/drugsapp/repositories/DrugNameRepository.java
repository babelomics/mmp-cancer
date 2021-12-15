package com.fujitsu.drugsapp.repositories;

import com.fujitsu.drugsapp.entities.DrugName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DrugNameRepository extends JpaRepository<DrugName, UUID> {
}
