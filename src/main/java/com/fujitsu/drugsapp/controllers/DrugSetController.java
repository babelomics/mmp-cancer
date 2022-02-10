package com.fujitsu.drugsapp.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fujitsu.drugsapp.entities.Drug;
import com.fujitsu.drugsapp.entities.DrugSet;
import com.fujitsu.drugsapp.entities.DrugUpdate;
import com.fujitsu.drugsapp.services.DrugSetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/drugSets")
@RequiredArgsConstructor
@Tag(name = "DrugSets", description = "Implemented Endpoints to deal with DrugSets")
public class DrugSetController {

    @Autowired
    private DrugSetService drugSetService;
    private boolean processing = false;

    private final DrugsAPIController panDrugsController = new DrugsAPIController();

    @Operation(summary = "Retrieve DrugSets", description = "List DrugSets, searching by %searchText%", tags = { "drugsets" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DrugSet.class)))) })
    @GetMapping()
    @CrossOrigin
    public ResponseEntity<List<DrugSet>> getAllDrugSets(@Parameter(description="Search Text to filter DrugSets, empty by default") @RequestParam(required = false) String searchText) {
        return new ResponseEntity<>(drugSetService.findAll(searchText), HttpStatus.OK);
    }

    @Operation(summary = "Filter by Id", description = "Retrieve a specific DrugSet by %id%", tags = { "drugsetId" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DrugSet.class)))),
            @ApiResponse(responseCode = "404", description = "drugset not found") })
    @GetMapping("/{id}")
    @CrossOrigin
    public ResponseEntity<DrugSet> getDrugSetById(@PathVariable("id") String id) {
        try {
            return new ResponseEntity<>(drugSetService.findById(UUID.fromString(id)), HttpStatus.OK);
        }catch (IllegalArgumentException illegalArgumentException){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "DrugSet Not Found", illegalArgumentException);
        }
    }

    @Operation(summary = "DrugSet updates by Id", description = "Retrieve all the updates related to a specific DrugSet by %id%", tags = { "drugsetUpdates" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DrugSet.class)))),
            @ApiResponse(responseCode = "404", description = "drugset not found") })
    @GetMapping("/{id}/updates")
    @CrossOrigin
    public ResponseEntity<List<DrugUpdate>> getDrugSetUpdates(@PathVariable("id") String id) {
        try {
            return new ResponseEntity<>(drugSetService.getDrugSetUpdates(UUID.fromString(id)), HttpStatus.OK);
        } catch (IllegalArgumentException illegalArgumentException){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "DrugSet Not Found", illegalArgumentException);
        }
    }

    @Operation(summary = "Retrieve Drugs from a DrugSet", description = "Retrieve drugs that belong to a specific DrugSet by %id%, filtering by both %searchText% and %date%", tags = { "drugsByDrugset" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DrugSet.class)))),
            @ApiResponse(responseCode = "404", description = "drugset not found") })
    @GetMapping("/{id}/drugs")
    @CrossOrigin
    public ResponseEntity<List<Drug>> getDrugsInDrugSet(@PathVariable("id") String id, @Parameter(description="Search Text to filter Drugs, empty by default") @RequestParam(required = false) String searchText,
                                                        @Parameter(description="Date to filter Drugs, empty by default") @RequestParam(name = "date", required = false) Instant date) {

        try {
            return new ResponseEntity<>(drugSetService.findDrugsById(UUID.fromString(id), searchText, date), HttpStatus.OK);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "DrugSet Not Found", illegalArgumentException);
        }
    }

    @Operation(summary = "Insert/Update Pandrugs set", description = "Endpoint to insert and/or update the whole set of drugs coming from Pandrugs", tags = { "pandrugsSet" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DrugSet.class)))) })
    @PostMapping("/pandrugs/updates")
    @CrossOrigin
    public ResponseEntity<DrugSet> updatePandrugSet() throws JsonProcessingException {

        DrugSet drugSet = new DrugSet();

        if(!processing) {

            processing = true;
            drugSet = panDrugsController.getAllDrugs();

            System.out.print("Pandrug data received!");

            if (!drugSetService.existByName(drugSet)) {
                drugSetService.saveDrugSet(drugSet);
            } else {
                List<Drug> drugs = drugSet.getDrugs();
                drugSet = drugSetService.findByName(drugSet.getName());
                drugSet.setDrugs(drugs);
                drugSetService.updateDrugSet(drugSet);
            }

            processing = false;

        }

        return new ResponseEntity<>(drugSetService.findById(drugSet.getId()), HttpStatus.OK);
    }

}
