package com.fujitsu.mmp.msusermanagement.controllers;

import com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.DiagnosticPanelDTO;
import com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.DiagnosticPanelTabsDTO;
import com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.filters.FilterDiagnosticPanelDTO;
import com.fujitsu.mmp.msusermanagement.services.DiagnosticPanelService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;

@CrossOrigin(origins = "*", maxAge = 3600, exposedHeaders = "Authorization")
@RequestMapping("/api/diagnosticPanels")
@RestController
public class DiagnosticPanelController {

    @Autowired
    DiagnosticPanelService diagnosticPanelService;

    /**
     * List all panel sets with pagination
     * @return list of all panel set entities found
     */
    @GetMapping("{diagnosticPanelSetIdentifier}/list")
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
    public ResponseEntity<Page<DiagnosticPanelDTO>> listDiagnosticPanelSet (@ApiIgnore("Ignored because swagger ui shows the wrong params, instead they are explained in the implicit params") Pageable pageable, FilterDiagnosticPanelDTO filterDiagnosticPanelDTO, @PathVariable String diagnosticPanelSetIdentifier) {
        return diagnosticPanelService.listDiagnosticPanelSet(pageable, filterDiagnosticPanelDTO, diagnosticPanelSetIdentifier);
    }

    /**
     * Create a new DiagnosticPanelSet
     * @param diagnosticPanelTabsDTO diagnosticPanelDTO to create
     * @return
     */
    @PostMapping("/diagnosticPanel")
    public ResponseEntity<DiagnosticPanelDTO> createDiagnosticPanel(@RequestBody DiagnosticPanelTabsDTO diagnosticPanelTabsDTO, HttpServletRequest httpServletRequest) {
        return diagnosticPanelService.createDiagnosticPanel(diagnosticPanelTabsDTO, httpServletRequest);
    }

    /**
     * Update an diagnosticPanel entity
     * @param guid: identifier of the entity
     * @param diagnosticPanelTabsDTO entity to update
     * @return DiagnosticPanelSetDTO updated
     */
    @PutMapping("diagnosticPanel/id/{guid}")
    public ResponseEntity<DiagnosticPanelDTO> update(@PathVariable String guid, @RequestBody DiagnosticPanelTabsDTO diagnosticPanelTabsDTO) {
        return diagnosticPanelService.update(guid, diagnosticPanelTabsDTO);
    }

    /**
     * Get an element from the list
     * @param guid: identifier of the entity to retrieve
     * @return DiagnosticPanelSetDTO found
     */
    @GetMapping("diagnosticPanel/id/{guid}")
    public ResponseEntity<DiagnosticPanelDTO> findByIdentifier(@PathVariable String guid) {
        return diagnosticPanelService.findByIdentifier(guid);
    }

    /**
     * Get details of the diagnostic panel tabs.
     * @param guid: identifier of the entity to retrieve.
     * @return
     */
    @GetMapping("diagnosticPanel/id/{guid}/tabs")
    public ResponseEntity<DiagnosticPanelTabsDTO> getDiagnosticPanelTabs(@PathVariable String guid){
        return diagnosticPanelService.getDiagnosticPanelTabs(guid);
    }

    /**
     * Delete a diagnostic panel.
     * @param diagnosticPanelSetIdentifier
     * @param guid
     * @return
     */
    @DeleteMapping("id/{diagnosticPanelSetIdentifier}/diagnosticPanel/id/{guid}")
    public ResponseEntity<Void> deleteDiagnosticPanel(@PathVariable String diagnosticPanelSetIdentifier, @PathVariable String guid, @RequestParam Boolean toDeleteChildren){
        return diagnosticPanelService.deleteDiagnosticPanel(diagnosticPanelSetIdentifier, guid, toDeleteChildren);
    }

}
