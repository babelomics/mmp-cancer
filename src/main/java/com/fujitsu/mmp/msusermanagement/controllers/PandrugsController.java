package com.fujitsu.mmp.msusermanagement.controllers;

import com.fujitsu.mmp.msusermanagement.dto.pandrugsapi.PandrugsConfigurationDTO;
import com.fujitsu.mmp.msusermanagement.services.PandrugService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/pandrugs")
@RestController
public class PandrugsController {

    @Autowired
    PandrugService pandrugService;

    /**
     * Create a user in Pandrugs
     * @param pandrugsConfigurationDTO
     * @return
     */
    @PostMapping("/validate")
    public ResponseEntity<?> createUser(@RequestBody PandrugsConfigurationDTO pandrugsConfigurationDTO) {
        return pandrugService.validate(pandrugsConfigurationDTO);
    }

    /**
     * Register a user in Pandrugs
     * @param pandrugsConfigurationDTO
     * @return
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody PandrugsConfigurationDTO pandrugsConfigurationDTO) {
        return pandrugService.register(pandrugsConfigurationDTO);
    }

}



