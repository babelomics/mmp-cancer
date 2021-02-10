package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.entities.Variant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VariantRepository extends MongoRepository<Variant, String> {

    Variant findByVariantIdentifier (String variantIdentifier);
}
