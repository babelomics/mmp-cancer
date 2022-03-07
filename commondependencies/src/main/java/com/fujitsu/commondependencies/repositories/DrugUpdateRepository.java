package com.fujitsu.commondependencies.repositories;

import com.fujitsu.commondependencies.entities.DrugUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DrugUpdateRepository extends JpaRepository<DrugUpdate, UUID> {
}
