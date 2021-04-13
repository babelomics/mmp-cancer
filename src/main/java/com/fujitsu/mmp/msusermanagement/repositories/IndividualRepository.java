package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.dto.individual.filters.FilterIndividualDTO;
import com.fujitsu.mmp.msusermanagement.dto.user.UserPermissionDTO;
import com.fujitsu.mmp.msusermanagement.entities.Individual;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndividualRepository extends MongoRepository<Individual, String>, IndividualRepositoryCustom{
    boolean existsByIndividualId(String individualId);

    Individual findByGuid(String guid);

    Individual findByIndividualId(String individualId);
}
