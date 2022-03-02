package com.fujitsu.drugsapp.repositories;

import com.fujitsu.drugsapp.entities.JobSynchronization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface JobSynchronizationRepository extends JpaRepository<JobSynchronization, UUID> {

    @Query("FROM JobSynchronization where status=?1")
    List<JobSynchronization> findJobs(String status);

}
