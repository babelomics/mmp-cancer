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

import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// 2021-12-15T12:36:19.296020263Z

@RestController
@RequestMapping("/drugSets")
@RequiredArgsConstructor
public class DrugSetController {

    @Autowired
    private DrugSetService drugSetService;

    private DrugsAPIController panDrugsController = new DrugsAPIController();

    @GetMapping()
    @CrossOrigin
    public ResponseEntity<List<DrugSet>> getAllDrugSets(@RequestParam(required = false) String searchText) throws JsonProcessingException {
        //DrugSet drugSet = panDrugsController.getAllDrugs();
        //drugSetService.saveDrugSet(drugSet);

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

        return new ResponseEntity<List<Drug>>(drugSetService.findDrugsById(UUID.fromString(id), searchText, date), HttpStatus.OK);
    }

    @PostMapping("/new")
    @ResponseBody
    public ResponseEntity<DrugSet> createDrugSet(@RequestBody DrugSet drugSet) throws URISyntaxException {
        return new ResponseEntity<>(drugSetService.saveDrugSet(drugSet), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<DrugUpdate> updateDrugSet(@RequestBody DrugSet drugSet) {
        if (drugSetService.existById(drugSet.getId())) {
            return new ResponseEntity<>(drugSetService.updateDrugSet(drugSet), HttpStatus.ACCEPTED);
        }
        throw new IllegalArgumentException("DrugSet with id " + drugSet.getId() + "not found");
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<HttpStatus> deleteDrugSet(@PathVariable("id") String id) {
        if (drugSetService.existById(UUID.fromString(id))) {
            drugSetService.deleteDrugSet(UUID.fromString(id));
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        throw new IllegalArgumentException("DrugSet with id " + id + "not found");
    }

}
