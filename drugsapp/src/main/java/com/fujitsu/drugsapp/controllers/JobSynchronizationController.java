package com.fujitsu.drugsapp.controllers;

import com.fujitsu.commondependencies.entities.JobSynchronization;
import com.fujitsu.commondependencies.services.JobSynchronizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@Tag(name = "JobSynchronization", description = "Implemented Endpoints to show the information about jobs")
public class JobSynchronizationController {

    @Autowired
    JobSynchronizationService jobSynchronizationService;

    @Operation(summary = "Retrieve Jobs", description = "List all Jobs", tags = { "listJobs" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = JobSynchronization.class)))) })
    @GetMapping()
    @CrossOrigin
    public ResponseEntity<List<JobSynchronization>> getAllDrugs(){
        return new ResponseEntity<>(jobSynchronizationService.getAllJobs(), HttpStatus.OK);
    }

}
