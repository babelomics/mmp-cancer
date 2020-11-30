package com.fujitsu.mmp.msusermanagement.controllers;

import com.fujitsu.mmp.msusermanagement.dto.cellbaseapi.CellBaseDTO;
import com.fujitsu.mmp.msusermanagement.services.CellBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/cellbase")
public class CellBaseController {
    @Autowired
    CellBaseService cellBaseService;

    /**
     * Check the status of the genetic dictionary service.
     * @param cellBaseDTO: URL of the genetic dictionary service.
     * @return
     */
    @PostMapping("/validate")
    public ResponseEntity<CellBaseDTO> validate(@RequestBody CellBaseDTO cellBaseDTO){
        return cellBaseService.validate(cellBaseDTO);
    }
}
