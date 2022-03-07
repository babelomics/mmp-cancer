package com.fujitsu.commondependencies.repositories;

import com.fujitsu.commondependencies.entities.DrugName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DrugNameRepository extends JpaRepository<DrugName, UUID> {
}
