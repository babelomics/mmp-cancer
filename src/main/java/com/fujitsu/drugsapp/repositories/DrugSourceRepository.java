package com.fujitsu.drugsapp.repositories;

import com.fujitsu.drugsapp.entities.DrugSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DrugSourceRepository extends JpaRepository<DrugSource, UUID> {
}
