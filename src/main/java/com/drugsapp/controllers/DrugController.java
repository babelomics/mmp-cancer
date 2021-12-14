package com.drugsapp.controllers;

import com.drugsapp.entities.Drug;
import com.drugsapp.services.DrugService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/drugs")
@RequiredArgsConstructor
public class DrugController {

    private final DrugService drugService;

    @GetMapping()
    public ResponseEntity<List<Drug>> getAllDrugs(){
        return new ResponseEntity<>(drugService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Drug> getDrugSetById(@PathVariable("id") String id) {
        return new ResponseEntity<>(drugService.findById(UUID.fromString(id)), HttpStatus.OK);
    }

    @PostMapping("/new")
    public ResponseEntity<Drug> createDrugSet(@ModelAttribute Drug drug) throws URISyntaxException {
        return new ResponseEntity<>(drugService.saveDrug(drug), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Drug> updateDrug(@PathVariable("id") String id, @RequestBody Drug drug) {
        if (drugService.existById(UUID.fromString(id))) {
            return new ResponseEntity<>(drugService.saveDrug(drug), HttpStatus.ACCEPTED);
        }
        throw new IllegalArgumentException("DrugSet with id " + id + "not found");
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteDrug(@PathVariable("id") String id) {
        drugService.deleteDrug(UUID.fromString(id));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
