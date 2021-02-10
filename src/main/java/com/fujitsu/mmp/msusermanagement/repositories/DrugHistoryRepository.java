package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.entities.DrugHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrugHistoryRepository extends MongoRepository<DrugHistory, String>, DrugCustomRepository {
    List<DrugHistory> findAllByStandardName(String standardName);
}
