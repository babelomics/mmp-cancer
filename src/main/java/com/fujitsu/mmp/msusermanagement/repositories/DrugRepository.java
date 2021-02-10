package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.entities.Drug;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrugRepository extends MongoRepository<Drug, String>, DrugCustomRepository {

    List<Drug> findByStandardNameIn (List<String> standardName);

    Drug findByStandardName(String standardName);
}
