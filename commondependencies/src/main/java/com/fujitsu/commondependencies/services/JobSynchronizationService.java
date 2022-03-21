package com.fujitsu.commondependencies.services;

import com.fujitsu.commondependencies.entities.JobSynchronization;
import com.fujitsu.commondependencies.repositories.JobSynchronizationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class JobSynchronizationService {
    private JobSynchronizationRepository jobSynchronizationRepository;

    public void save(JobSynchronization jobSynchronization){
        jobSynchronizationRepository.save(jobSynchronization);
    }

    public void update(JobSynchronization jobSynchronization) { jobSynchronizationRepository.save(jobSynchronization); }

    public List<JobSynchronization> getAllJobs() { return jobSynchronizationRepository.findAll(); }


}
