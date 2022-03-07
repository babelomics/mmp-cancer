package com.fujitsu.commondependencies.repositories;

import com.fujitsu.commondependencies.entities.DrugSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DrugSourceRepository extends JpaRepository<DrugSource, UUID> {
}
