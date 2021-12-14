package com.drugsapp.repositories;

import com.drugsapp.entities.Drug;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DrugRepository extends JpaRepository<Drug, UUID> {
}
