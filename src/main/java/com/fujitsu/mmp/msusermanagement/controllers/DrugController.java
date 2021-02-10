package com.fujitsu.mmp.msusermanagement.controllers;



import com.fujitsu.mmp.msusermanagement.dto.drug.AvailabilityDrugDTO;
import com.fujitsu.mmp.msusermanagement.dto.drug.DrugDTO;
import com.fujitsu.mmp.msusermanagement.dto.drug.filters.FilterDrugDTO;
import com.fujitsu.mmp.msusermanagement.entities.Drug;
import com.fujitsu.mmp.msusermanagement.services.DrugService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600, exposedHeaders = "Authorization")
@RequestMapping("/api/drugs")
@RestController
public class DrugController {
    @Autowired
    private DrugService drugService;

    /**
     * List all drugs with pagination.
     * @param filterDrugDTO: filter object.
     * @return list of all drugs entities found
     */
    @GetMapping("/list")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "Results page you want to retrieve (0..N)", defaultValue = "0"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "Number of records per page.", defaultValue = "20"),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "Sorting criteria in the format: property(,asc|desc). " +
                            "Default sort order is ascending. " +
                            "Multiple sort criteria are supported.")
    })
    public ResponseEntity<Page<DrugDTO>> findAllByPage(@ApiIgnore("Ignored because swagger ui shows the wrong params, instead they are explained in the implicit params") Pageable pageable, FilterDrugDTO filterDrugDTO) {
        return drugService.findAllByPage(pageable, filterDrugDTO);
    }

    /**
     * Change the availability of the provided drugs.
     * @param availabilityDrugDTO: List of
     * @return
     */
    @PutMapping("/updateAvailability")
    public ResponseEntity<?> setAvailable (@RequestBody AvailabilityDrugDTO availabilityDrugDTO, HttpServletRequest httpServletRequest) {
        return drugService.setAvailable(availabilityDrugDTO, httpServletRequest);
    }

    /**
     *
     * @return
     */
    @PostMapping("/manual-drug-update")
    public List<Drug> createOrUpdate() {
        return drugService.createOrUpdateDrugs();
    }

    @GetMapping("drug/id/{id}")
    public ResponseEntity<List<DrugDTO>> findByDrugId (@PathVariable String id){
        return drugService.findByDrugId(id);
    }

    /**
     * Update a drug entity
     * @param drugDTO entity to update
     * @return userDTO updated
     */
    @PutMapping("/drug")
    public ResponseEntity<DrugDTO> update(@RequestBody DrugDTO drugDTO) {
        return drugService.update(drugDTO);
    }

}
