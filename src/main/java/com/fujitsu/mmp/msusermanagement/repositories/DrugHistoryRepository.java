package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.entities.DrugHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DrugHistoryRepository extends MongoRepository<DrugHistory, String>, DrugCustomRepository {
    List<DrugHistory> findAllByStandardName(String standardName);
}
