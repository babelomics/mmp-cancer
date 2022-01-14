package com.fujitsu.drugsapp.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fujitsu.drugsapp.entities.Drug;
import com.fujitsu.drugsapp.entities.DrugSet;
import com.fujitsu.drugsapp.entities.DrugUpdate;
import com.fujitsu.drugsapp.services.DrugSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// 2021-12-15T12:36:19.296020263Z

@RestController
@RequestMapping("/drugSets")
@RequiredArgsConstructor
public class DrugSetController {

    @Autowired
    private DrugSetService drugSetService;

    private final DrugsAPIController panDrugsController = new DrugsAPIController();

    @GetMapping()
    @CrossOrigin
    public ResponseEntity<List<DrugSet>> getAllDrugSets(@RequestParam(required = false) String searchText) throws JsonProcessingException {
        return new ResponseEntity<>(drugSetService.findAll(searchText), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @CrossOrigin
    public ResponseEntity<DrugSet> getDrugSetById(@PathVariable("id") String id) {
        return new ResponseEntity<>(drugSetService.findById(UUID.fromString(id)), HttpStatus.OK);
    }

    @GetMapping("/{id}/updates")
    public ResponseEntity<List<DrugUpdate>> getDrugSetUpdates(@PathVariable("id") String id) {
        return new ResponseEntity<>(drugSetService.getDrugSetUpdates(UUID.fromString(id)), HttpStatus.OK);
    }

    @GetMapping("/{id}/drugs")
    @CrossOrigin
    public ResponseEntity<List<Drug>> getDrugsInDrugSet(@PathVariable("id") String id, @RequestParam(required = false) String searchText,
                                                        @RequestParam(name = "date", required = false) Instant date) {

        return new ResponseEntity<>(drugSetService.findDrugsById(UUID.fromString(id), searchText, date), HttpStatus.OK);
    }

    @GetMapping("/getPandrugSet")
    public ResponseEntity<DrugSet> getPandrugSet() throws JsonProcessingException {
        DrugSet drugSet = panDrugsController.getAllDrugs();

        System.out.print("Pandrug data received!");

        if(!drugSetService.existByName(drugSet)){
            drugSetService.saveDrugSet(drugSet);
        }else{
            List<Drug> drugs = drugSet.getDrugs();
            drugSet = drugSetService.findByName(drugSet.getName());
            drugSet.setDrugs(drugs);
            drugSetService.updateDrugSet(drugSet);
        }

        return new ResponseEntity<>(drugSetService.findById(drugSet.getId()), HttpStatus.OK);
    }


    @PostMapping("/{id}/update")
    public ResponseEntity<DrugSet> updateDrugSet(@RequestBody DrugSet drugSet) {
        if (drugSetService.existById(drugSet.getId())) {
            return new ResponseEntity<>(drugSetService.updateDrugSet(drugSet), HttpStatus.ACCEPTED);
        }
        throw new IllegalArgumentException("DrugSet with id " + drugSet.getId() + "not found");
    }

}
