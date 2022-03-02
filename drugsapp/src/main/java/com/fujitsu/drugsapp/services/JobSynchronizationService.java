package com.fujitsu.drugsapp.services;

import com.fujitsu.drugsapp.entities.JobSynchronization;
import com.fujitsu.drugsapp.repositories.JobSynchronizationRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class JobSynchronizationService {
    private JobSynchronizationRepository jobSynchronizationRepository;

    public void save(JobSynchronization jobSynchronization){
        jobSynchronizationRepository.save(jobSynchronization);
    }

    public List<JobSynchronization> getWaitingJobs(){
        return jobSynchronizationRepository.findJobs("Waiting");
    }



}
