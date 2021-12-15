package com.fujitsu.drugsapp.controllers;

import com.fujitsu.drugsapp.entities.Drug;
import com.fujitsu.drugsapp.entities.DrugSet;
import com.fujitsu.drugsapp.services.DrugSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

// 2021-12-15T12:36:19.296020263Z

@RestController
@RequestMapping("/drugSets")
@RequiredArgsConstructor
public class DrugSetController {

    @Autowired
    private DrugSetService drugSetService;

    @GetMapping()
    public ResponseEntity<List<DrugSet>> getAllDrugSets(@RequestParam(required = false) String searchText){
        return new ResponseEntity<>(drugSetService.findAll(searchText), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DrugSet> getDrugSetById(@PathVariable("id") String id) {
        return new ResponseEntity<>(drugSetService.findById(UUID.fromString(id)), HttpStatus.OK);
    }

    @GetMapping("/{id}/drugs")
    public ResponseEntity<List<Drug>> getDrugsInDrugSet(@PathVariable("id") String id, @RequestParam(required = false) String searchText, @RequestParam(name = "date", required = false) Instant date) {

        if(date!=null) {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(date, ZoneOffset.UTC);
            return new ResponseEntity<List<Drug>>(drugSetService.findDrugsById(UUID.fromString(id), searchText, localDateTime), HttpStatus.OK);
        }else{
            return new ResponseEntity<List<Drug>>(drugSetService.findDrugsById(UUID.fromString(id), searchText, LocalDateTime.ofInstant(date, ZoneOffset.UTC)), HttpStatus.OK);
        }

    }

    @PostMapping("/new")
    @ResponseBody
    public ResponseEntity<DrugSet> createDrugSet(@RequestBody DrugSet drugSet) throws URISyntaxException {
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
