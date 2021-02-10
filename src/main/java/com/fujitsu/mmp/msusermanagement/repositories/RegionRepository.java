package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.entities.Region;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends MongoRepository<Region, String> {

    Region findByRegionIdentifier (String regionIdentifier);
}
