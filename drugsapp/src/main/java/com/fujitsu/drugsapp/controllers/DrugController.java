package com.fujitsu.drugsapp.controllers;

import com.fujitsu.drugsapp.entities.Drug;
import com.fujitsu.drugsapp.entities.DrugSet;
import com.fujitsu.drugsapp.services.DrugService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/drugs")
@RequiredArgsConstructor
@Tag(name = "Drugs", description = "Implemented Endpoints to deal with Drugs")
public class DrugController {

    private final DrugService drugService;
    private final DrugSetService drugSetService;

    @Operation(summary = "Retrieve Drugs", description = "List all Drugs, filtering by %drugSetId%, %searchText% and %date%", tags = { "listDrugs" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DrugSet.class)))) })
    @GetMapping()
    public ResponseEntity<List<Drug>> getAllDrugs(@Parameter(description="DrugSet Id to filter Drugs, empty by default") @RequestParam(required = false) String drugSetId,
                                                  @Parameter(description="Search Text to filter Drugs, empty by default")@RequestParam(required = false) String searchText,
                                                  @Parameter(description="Date to filter Drugs, empty by default") @RequestParam(name = "date", required = false) Instant date){

        if(drugSetId==null){
            return new ResponseEntity<>(drugService.findAll(), HttpStatus.OK);
        }else {
            return new ResponseEntity<>(drugSetService.findDrugsById(UUID.fromString(drugSetId), searchText, date), HttpStatus.OK);
        }
    }

    @Operation(summary = "Retrieve a specific Drug", description = "Retrieve a specific Drug by %id%", tags = { "drugId" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DrugSet.class)))) ,
            @ApiResponse(responseCode = "404", description = "drug not found") })
    @GetMapping("/{id}")
    public ResponseEntity<Drug> getDrugSetById(@PathVariable("id") String id) {
        try {
            return new ResponseEntity<>(drugService.findById(UUID.fromString(id)), HttpStatus.OK);
        } catch (IllegalArgumentException illegalArgumentException){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "DrugSet Not Found", illegalArgumentException);
        }
    }
}
