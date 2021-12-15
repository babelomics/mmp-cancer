package com.fujitsu.drugsapp.repositories;

import com.fujitsu.drugsapp.entities.Drug;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DrugRepository extends JpaRepository<Drug, UUID> {
    boolean existsByStandardName(Drug drug);
}
