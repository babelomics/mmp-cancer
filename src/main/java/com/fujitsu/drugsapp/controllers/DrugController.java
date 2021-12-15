package com.fujitsu.drugsapp.controllers;

import com.fujitsu.drugsapp.entities.Drug;
import com.fujitsu.drugsapp.services.DrugService;
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

    @PutMapping("/{id}")
    public ResponseEntity<Drug> updateDrug(@PathVariable("id") String id, @RequestBody Drug drug) {
        if (drugService.existById(UUID.fromString(id))) {
            return new ResponseEntity<>(drugService.saveDrug(drug), HttpStatus.ACCEPTED);
        }
        throw new IllegalArgumentException("DrugSet with id " + id + "not found");
    }
}
