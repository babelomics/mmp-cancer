package com.fujitsu.updatesets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fujitsu.commondependencies.repositories.JobSynchronizationRepository;
import com.fujitsu.commondependencies.springBatch.AddUpdateJobConfig;
import com.fujitsu.commondependencies.entities.JobSynchronization;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class UpdateInsertService {

    @Autowired
    private JobSynchronizationRepository jobSynchronizationRepository;

    @Autowired
    private AddUpdateJobConfig addUpdateJobConfig;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    ShutdownManager shutdownManager;

    public void checkForUpdate() throws JsonProcessingException{

        System.out.print("Checking for Waiting jobs...");
        List<JobSynchronization> jobSynchronizationList = jobSynchronizationRepository.findJobs("Waiting");

        if (jobSynchronizationList.size() > 0) {
            for (JobSynchronization jobSynchronization : jobSynchronizationList) {
                Job job = addUpdateJobConfig.processUpdate(jobSynchronization);

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

        shutdownManager.initiateShutdown(0);

    }
}