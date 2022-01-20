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
    public ResponseEntity<List<DrugSet>> getAllDrugSets(@Parameter(description="Search Text to filter DrugSets, empty by default") @RequestParam(required = false) String searchText) throws JsonProcessingException {
        return new ResponseEntity<>(drugSetService.findAll(searchText), HttpStatus.OK);
    }

    @Operation(summary = "Filter by Id", description = "Retrieve a specific DrugSet by %id%", tags = { "drugsetId" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DrugSet.class)))) })
    @GetMapping("/{id}")
    @CrossOrigin
    public ResponseEntity<DrugSet> getDrugSetById(@PathVariable("id") String id) {
        return new ResponseEntity<>(drugSetService.findById(UUID.fromString(id)), HttpStatus.OK);
    }

    @Operation(summary = "DrugSet updates by Id", description = "Retrieve all the updates related to a specific DrugSet by %id%", tags = { "drugsetUpdates" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DrugSet.class)))) })
    @GetMapping("/{id}/updates")
    public ResponseEntity<List<DrugUpdate>> getDrugSetUpdates(@PathVariable("id") String id) {
        return new ResponseEntity<>(drugSetService.getDrugSetUpdates(UUID.fromString(id)), HttpStatus.OK);
    }

    @Operation(summary = "Retrieve Drugs from a DrugSet", description = "Retrieve drugs that belong to a specific DrugSet by %id%, filtering by both %searchText% and %date%", tags = { "drugsByDrugset" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DrugSet.class)))) })
    @GetMapping("/{id}/drugs")
    @CrossOrigin
    public ResponseEntity<List<Drug>> getDrugsInDrugSet(@PathVariable("id") String id, @Parameter(description="Search Text to filter Drugs, empty by default") @RequestParam(required = false) String searchText,
                                                        @Parameter(description="Date to filter Drugs, empty by default") @RequestParam(name = "date", required = false) Instant date) {

        return new ResponseEntity<>(drugSetService.findDrugsById(UUID.fromString(id), searchText, date), HttpStatus.OK);
    }

    @Operation(summary = "Insert/Update Pandrugs set", description = "Endpoint to insert and/or update the whole set of drugs coming from Pandrugs", tags = { "pandrugsSet" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DrugSet.class)))) })
    @GetMapping("/getPandrugSet")
    public ResponseEntity<DrugSet> getPandrugSet() throws JsonProcessingException {


        if(!processing) {

                processing = true;
                DrugSet drugSet = panDrugsController.getAllDrugs();

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
                return new ResponseEntity<>(drugSetService.findById(drugSet.getId()), HttpStatus.OK);
        }

        return null;
    }


    @Operation(summary = "Update a specific DrugSet by Id", description = "Endpoint to update a concrete DrugSet by %id%", tags = { "updateDrugset" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DrugSet.class)))) })
    @PostMapping("/{id}/update")
    public ResponseEntity<DrugSet> updateDrugSet(@RequestBody DrugSet drugSet) {
        if (drugSetService.existById(drugSet.getId())) {
            return new ResponseEntity<>(drugSetService.updateDrugSet(drugSet), HttpStatus.ACCEPTED);
        }
        throw new IllegalArgumentException("DrugSet with id " + drugSet.getId() + "not found");
    }

}
