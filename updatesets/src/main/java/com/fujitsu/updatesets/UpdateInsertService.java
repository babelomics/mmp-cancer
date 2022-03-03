package com.fujitsu.updatesets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fujitsu.drugsapp.controllers.DrugsAPIController;
import com.fujitsu.drugsapp.entities.Drug;
import com.fujitsu.drugsapp.entities.DrugSet;
import com.fujitsu.drugsapp.entities.JobSynchronization;
import com.fujitsu.drugsapp.repositories.JobSynchronizationRepository;
import com.fujitsu.drugsapp.services.DrugSetService;
import com.fujitsu.drugsapp.springBatch.AddUpdateJobConfig;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UpdateInsertService {


    @Autowired
    private JobSynchronizationRepository jobSynchronizationRepository;

    @Autowired
    private AddUpdateJobConfig addUpdateJobConfig;

    @Autowired
    private JobLauncher jobLauncher;

    @Scheduled(fixedRate=30000)
    public void checkForUpdate() throws JsonProcessingException {
        List<JobSynchronization> jobSynchronizationList = jobSynchronizationRepository.findJobs("Waiting");

        if (jobSynchronizationList.size() > 0) {
            Job job = addUpdateJobConfig.processUpdate(jobSynchronizationList.get(0));

            try {
                jobLauncher.run(job, new JobParametersBuilder()
                        .addLong("timestamp",
                                System.currentTimeMillis())
                        .toJobParameters());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}