package com.fujitsu.commondependencies.springBatch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fujitsu.commondependencies.controllers.DrugsAPIController;
import com.fujitsu.commondependencies.entities.Drug;
import com.fujitsu.commondependencies.services.DrugSetService;
import com.fujitsu.commondependencies.services.JobSynchronizationService;
import com.fujitsu.commondependencies.entities.DrugSet;
import com.fujitsu.commondependencies.entities.JobSynchronization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.UUID;

@Slf4j
@Configuration
@EnableBatchProcessing
@EnableScheduling
public class AddUpdateJobConfig {

    public static final String TASKLET_STEP = "taskletStep";

    @Autowired
    private DrugSetService drugSetService;

    @Autowired
    private JobSynchronizationService jobSynchronizationService;

    private static final String JOB_NAME = "addUpdateJob";

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobBuilderFactory jobBuilders;

    @Autowired
    private StepBuilderFactory stepBuilders;

    @Autowired
    private JobExplorer jobs;

    @PreDestroy
    public void destroy() throws NoSuchJobException {
        jobs.getJobNames().forEach(name -> log.info("job name: {}", name));
        jobs.getJobInstances(JOB_NAME, 0, jobs.getJobInstanceCount(JOB_NAME)).forEach(
                jobInstance -> {
                    log.info("job instance id {}", jobInstance.getInstanceId());
                }
        );

    }

    public Job queueDrugsetJob(String drugSetName) {
        return jobBuilders.get(JOB_NAME)
                .start(waitingTaskletStep(drugSetName))
                .build();
    }

    public Job processUpdate(JobSynchronization jobSynchronization) throws JsonProcessingException {
        return jobBuilders.get(JOB_NAME)
                .start(runningTaskletStep(jobSynchronization))
                .build();
    }

    public Step waitingTaskletStep(String drugSetName) {
        return stepBuilders.get(TASKLET_STEP)
                .tasklet(waitingTasklet(drugSetName))
                .build();
    }

    public Step runningTaskletStep(JobSynchronization jobSynchronization) throws JsonProcessingException {
        return stepBuilders.get(TASKLET_STEP)
                .tasklet(runningTasklet(jobSynchronization))
                .build();
    }

    public Tasklet waitingTasklet(String drugSetName) {

        JobSynchronization jobSynchronization = new JobSynchronization();
        jobSynchronization.setStatus("Waiting");
        jobSynchronization.setDrugsetName(drugSetName);

        jobSynchronizationService.save(jobSynchronization);

        return (contribution, chunkContext) -> {
            return RepeatStatus.FINISHED;
        };
    }

    public Tasklet runningTasklet(JobSynchronization jobSynchronization) throws JsonProcessingException {

        jobSynchronization.setStatus("Running");
        jobSynchronizationService.update(jobSynchronization);

        try {
            DrugsAPIController panDrugsController = new DrugsAPIController();
            DrugSet drugSet = panDrugsController.getAllDrugs();
            System.out.print("Updating Pandrugs set!!!!");
            if (!drugSetService.existByName(drugSet)) {
                drugSetService.saveDrugSet(drugSet);
            } else {
                List<Drug> drugs = drugSet.getDrugs();
                drugSet = drugSetService.findByName(drugSet.getName());
                drugSet.setDrugs(drugs);
                drugSetService.updateDrugSet(drugSet);
            }

            jobSynchronization.setStatus("Complete");
        } catch (Exception e) {
            jobSynchronization.setStatus("Failed");
        } finally {
            jobSynchronizationService.update(jobSynchronization);
        }

        return (contribution, chunkContext) -> {
            return RepeatStatus.FINISHED;
        };
    }
}
