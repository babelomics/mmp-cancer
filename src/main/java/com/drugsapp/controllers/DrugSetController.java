package com.drugsapp.controllers;

import com.drugsapp.entities.DrugSet;
import com.drugsapp.services.DrugSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/drugSets")
@RequiredArgsConstructor
public class DrugSetController {

    private final DrugSetService drugSetService;

    @GetMapping()
    public ResponseEntity<List<DrugSet>> getAllDrugSets(){
        return new ResponseEntity<>(drugSetService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DrugSet> getDrugSetById(@PathVariable("id") String id, @RequestParam(required = false) String searchText) {
        return new ResponseEntity<>(drugSetService.findById(UUID.fromString(id), searchText), HttpStatus.OK);
    }

    @PostMapping("/new")
    public ResponseEntity<DrugSet> createDrugSet(@ModelAttribute DrugSet drugSet) throws URISyntaxException {
        return new ResponseEntity<>(drugSetService.saveDrugSet(drugSet), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<DrugSet> updateDrugSet(@PathVariable("id") String id, @RequestBody DrugSet drugSet) {
        if (drugSetService.existById(UUID.fromString(id))) {
            return new ResponseEntity<>(drugSetService.saveDrugSet(drugSet), HttpStatus.ACCEPTED);
        }
        throw new IllegalArgumentException("DrugSet with id " + id + "not found");
    }
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<HttpStatus> deleteDrugSet(@PathVariable("id") String id) {
        drugSetService.deleteDrugSet(UUID.fromString(id));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
