package com.fujitsu.commondependencies.repositories;

import com.fujitsu.commondependencies.entities.Drug;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DrugRepository extends JpaRepository<Drug, UUID> {
}
