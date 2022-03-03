package com.fujitsu.drugsapp.springBatch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fujitsu.drugsapp.controllers.DrugsAPIController;
import com.fujitsu.drugsapp.entities.Drug;
import com.fujitsu.drugsapp.entities.DrugSet;
import com.fujitsu.drugsapp.entities.JobSynchronization;
import com.fujitsu.drugsapp.repositories.DrugRepository;
import com.fujitsu.drugsapp.repositories.JobSynchronizationRepository;
import com.fujitsu.drugsapp.services.DrugSetService;
import com.fujitsu.drugsapp.services.JobSynchronizationService;
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
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.Flow;

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

    public Job queueDrugsetJob() {
        return jobBuilders.get(JOB_NAME)
                .start(waitingTaskletStep())
                .build();
    }

    public Job processUpdate(JobSynchronization jobSynchronization) throws JsonProcessingException {
        return jobBuilders.get(JOB_NAME)
                .start(runningTaskletStep(jobSynchronization))
                .build();
    }

    public Step waitingTaskletStep() {
        return stepBuilders.get(TASKLET_STEP)
                .tasklet(waitingTasklet())
                .build();
    }

    public Step runningTaskletStep(JobSynchronization jobSynchronization) throws JsonProcessingException {
        return stepBuilders.get(TASKLET_STEP)
                .tasklet(runningTasklet(jobSynchronization))
                .build();
    }

    public Tasklet waitingTasklet() {

        JobSynchronization jobSynchronization = new JobSynchronization();
        jobSynchronization.setStatus("Waiting");

        jobSynchronizationService.save(jobSynchronization);

        return (contribution, chunkContext) -> {
            return RepeatStatus.FINISHED;
        };
    }

    public Tasklet runningTasklet(JobSynchronization jobSynchronization) throws JsonProcessingException {

        jobSynchronization.setStatus("Running");

        jobSynchronizationService.update(jobSynchronization);

        DrugSet drugSet = new DrugSet();
        DrugsAPIController panDrugsController = new DrugsAPIController();
        drugSet = panDrugsController.getAllDrugs();

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
        jobSynchronizationService.update(jobSynchronization);

        return (contribution, chunkContext) -> {
            return RepeatStatus.FINISHED;
        };
    }

    @StepScope
    @Bean
    public ItemReader<DrugSet> reader() {
        return new UpdateItemReader();
    }

    @StepScope
    @Bean
    public ItemProcessor<DrugSet, DrugSet> processor() {
        final CompositeItemProcessor<DrugSet, DrugSet> processor = new CompositeItemProcessor<>();
        return processor;
    }

    @StepScope
    @Bean
    public ItemWriter<DrugSet> writer() {
        return new UpdateItemWriter();
    }

}
