package com.fujitsu.drugsapp.repositories;

import com.fujitsu.drugsapp.entities.DrugUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DrugUpdateRepository extends JpaRepository<DrugUpdate, UUID> {
}
