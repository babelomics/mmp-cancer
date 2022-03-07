package com.fujitsu.updatesets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fujitsu.commondependencies.repositories.JobSynchronizationRepository;
import com.fujitsu.commondependencies.springBatch.AddUpdateJobConfig;
import com.fujitsu.commondependencies.entities.JobSynchronization;
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