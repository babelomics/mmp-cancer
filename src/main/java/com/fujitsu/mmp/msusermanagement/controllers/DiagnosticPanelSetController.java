package com.fujitsu.mmp.msusermanagement.controllers;

import com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.DiagnosticPanelSetDTO;
import com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.filters.FilterDiagnosticPanelSetDTO;
import com.fujitsu.mmp.msusermanagement.services.DiagnosticPanelSetService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600, exposedHeaders = "Authorization")
@RequestMapping("/api/diagnosticPanelSets")
@RestController
public class DiagnosticPanelSetController {
    @Autowired
    DiagnosticPanelSetService diagnosticPanelSetService;

    /**
     * List all the panel sets
     * @return list of all the panel set entities found
     */
    @GetMapping("")
    public ResponseEntity<List<DiagnosticPanelSetDTO>> getAllPanelSets(){
        return diagnosticPanelSetService.findAll();
    }


    /**
     * List all panel sets with pagination
     * @return list of all panel set entities found
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
    public ResponseEntity<Page<DiagnosticPanelSetDTO>> listDiagnosticPanelSet (@ApiIgnore("Ignored because swagger ui shows the wrong params, instead they are explained in the implicit params") Pageable pageable, FilterDiagnosticPanelSetDTO filterDiagnosticPanelSetDTO) {
        return diagnosticPanelSetService.listDiagnosticPanelSet(pageable, filterDiagnosticPanelSetDTO);
    }

    /**
     * Get an element from the list
     * @param diagnosticPanelSetIdentifier: identifier of the entity to retrieve
     * @return DiagnosticPanelSetDTO found
     */
    @GetMapping("diagnosticPanelSet/id/{diagnosticPanelSetIdentifier}")
    public ResponseEntity<DiagnosticPanelSetDTO> findByIdentifier(@PathVariable String diagnosticPanelSetIdentifier) {
        return diagnosticPanelSetService.findByIdentifier(diagnosticPanelSetIdentifier);
    }

    /**
     * Update an HelloWorld entity
     * @param diagnosticPanelSetIdentifier: identifier of the entity
     * @param DiagnosticPanelSetDTO entity to update
     * @return DiagnosticPanelSetDTO updated
     */
    @PutMapping("diagnosticPanelSet/id/{diagnosticPanelSetIdentifier}")
    public ResponseEntity<DiagnosticPanelSetDTO> update(@PathVariable String diagnosticPanelSetIdentifier, @RequestBody DiagnosticPanelSetDTO DiagnosticPanelSetDTO) {
        return diagnosticPanelSetService.update(diagnosticPanelSetIdentifier, DiagnosticPanelSetDTO);
    }

    /**
     * Delete a diagnosticPanelSet entity
     * @param diagnosticPanelSetIdentifier identifier of diagnosticPanelSet entity
     * @return SuccessResponseDto with true if the element has been deleted
     */
    @DeleteMapping("/diagnosticPanelSet/id/{diagnosticPanelSetIdentifier}")
    public ResponseEntity<DiagnosticPanelSetDTO> delete(@PathVariable String diagnosticPanelSetIdentifier) {
        return diagnosticPanelSetService.delete(diagnosticPanelSetIdentifier);
    }


    /**
     * Create a new DiagnosticPanelSet
     * @param diagnosticPanelSetDTO diagnosticPanelSetDTO to create
     * @return
     */
    @PostMapping("/diagnosticPanelSet")
    public ResponseEntity<DiagnosticPanelSetDTO> createDiagnosticPanelSet(@RequestBody DiagnosticPanelSetDTO diagnosticPanelSetDTO, HttpServletRequest httpServletRequest) {
        return diagnosticPanelSetService.createDiagnosticPanelSet(diagnosticPanelSetDTO, httpServletRequest);
    }

    @GetMapping("/diagnosticPanelSet/id/{diagnosticPanelSetIdentifier}/export")
    public ResponseEntity<String> exportFile(@PathVariable String diagnosticPanelSetIdentifier) {
        return diagnosticPanelSetService.exportDiagnosticPanelSet(diagnosticPanelSetIdentifier);
    }

    /**
     * Create a new DiagnosticPanelSet
     * @param file diagnosticPanelSetDTO to create
     * @return
     */
    @PostMapping("/diagnosticPanelSet/sendFile")
    public ResponseEntity<String> importPanelSet (@RequestParam("file") MultipartFile file, @RequestParam String id, @RequestParam String name, @RequestParam Boolean isOverwritten) {
        return diagnosticPanelSetService.importPanelSet(file, id, name, isOverwritten);
    }
}

