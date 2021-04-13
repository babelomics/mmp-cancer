package com.fujitsu.mmp.msusermanagement.controllers;

import com.fujitsu.mmp.msusermanagement.dto.group.filters.FilterUsersPermissionsDTO;
import com.fujitsu.mmp.msusermanagement.dto.individual.IndividualDTO;
import com.fujitsu.mmp.msusermanagement.dto.individual.IndividualPermissionDTO;
import com.fujitsu.mmp.msusermanagement.dto.individual.filters.FilterIndividualDTO;
import com.fujitsu.mmp.msusermanagement.dto.permission.UserGroupPermissionDTO;
import com.fujitsu.mmp.msusermanagement.dto.permission.UserProjectPermissionDTO;
import com.fujitsu.mmp.msusermanagement.services.IndividualService;
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
@RequestMapping("/api/individuals")
@RestController
public class IndividualController {

    @Autowired
    IndividualService individualService;

    /**
     * List all individuals with pagination
     * @return list of all individuals entities found
     */
    @GetMapping("/project/id/{projectId}/individuals/list")
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
    public ResponseEntity<Page<IndividualDTO>> listIndividuals(@ApiIgnore("Ignored because swagger ui shows the wrong params, instead they are explained in the implicit params") Pageable pageable, FilterIndividualDTO filterIndividualDTO, HttpServletRequest httpServletRequest, @PathVariable String projectId) {
        return individualService.listIndividuals(pageable, filterIndividualDTO, httpServletRequest, projectId);
    }

    /**
     * Create a new Individual
     * @param individualDTO individualDTO to create
     * @return
     */
    @PostMapping("project/id/{projectId}/individual")
    public ResponseEntity<IndividualDTO> createIndividual(@RequestBody IndividualDTO individualDTO, @PathVariable String projectId, HttpServletRequest httpServletRequest) {
        return individualService.createIndividual(individualDTO, projectId, httpServletRequest);
    }

    /**
     * Get details of individuals.
     * @param individualId: guid of the entity to retrieve.
     * @return
     */
    @GetMapping("project/id/{projectId}/individual/id/{individualId}")
    public ResponseEntity<IndividualDTO> getIndividual(@PathVariable String individualId, @PathVariable String projectId){
        return individualService.getIndividual(individualId, projectId);
    }

    /**
     * Update an individual entity
     * @param individualId: identifier of the entity
     * @param individualDTO entity to update
     * @return individualDTO updated
     */
    @PutMapping("project/id/{projectId}/individual/id/{individualId}")
    public ResponseEntity<IndividualDTO> updateIndividual(@PathVariable String individualId, @PathVariable String projectId, @RequestBody IndividualDTO individualDTO, HttpServletRequest httpServletRequest) {
        return individualService.updateIndividual(individualId, projectId, individualDTO, httpServletRequest);
    }

    /**
     * Delete a individual.
     * @param individualId
     * @return
     */
    @DeleteMapping("project/id/{projectId}/individual/id/{individualId}")
    public ResponseEntity<IndividualDTO> deleteIndividual(@PathVariable String individualId, @PathVariable String projectId, HttpServletRequest httpServletRequest){
        return individualService.deleteIndividual(individualId, projectId, httpServletRequest);
    }

    /**
     * List all individuals permissions
     * @return list of all users with individual permissions
     */
    @GetMapping("individual/id/{individualId}/project/id/{projectId}/permissions/list")
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
    public ResponseEntity<Page<UserProjectPermissionDTO>> listIndividualsPermissions(@ApiIgnore("Ignored because swagger ui shows the wrong params, instead they are explained in the implicit params") Pageable pageable, FilterUsersPermissionsDTO filterUsersPermissionsDTO, HttpServletRequest httpServletRequest, @PathVariable String projectId, @PathVariable String individualId) {
        return individualService.listIndividualsPermissions(pageable, filterUsersPermissionsDTO, httpServletRequest, projectId, individualId);
    }

    /**
     * Create a new UserProjectPermissionDTO
     * @param individualPermissionDTO userProjectPermissionDTO to create
     * @return
     */
    @PostMapping("/project/id/{projectId}/permissions/add/user/id/{userId}")
    public ResponseEntity<UserProjectPermissionDTO> addIndividualPermissionsToUser(@RequestBody IndividualPermissionDTO individualPermissionDTO, @PathVariable String projectId, HttpServletRequest httpServletRequest) {
        return individualService.addIndividualPermissionsToUser(individualPermissionDTO, projectId, httpServletRequest);
    }

    /**
     *
     * @param
     * @return
     */
    @DeleteMapping("/individual/id/{individualId}/project/id/{projectId}/permissions/delete/user/id/{userId}")
    public ResponseEntity<Void> deleteIndividualPermissionsFromUser(@PathVariable String userId, @PathVariable String individualId,@PathVariable String projectId, HttpServletRequest httpServletRequest){
        return individualService.deleteIndividualPermissionsFromUser(userId, individualId, projectId, httpServletRequest);
    }

    /**
     * @param
     * @param
     */
    @GetMapping("/individual/id/{individualId}/project/id/{projectId}/user/id/{userId}")
    public ResponseEntity<UserGroupPermissionDTO> getUserIndividualPermissions(@PathVariable String userId, @PathVariable String projectId, @PathVariable String individualId, HttpServletRequest httpServletRequest){
        return individualService.getUserIndividualPermissions(userId, projectId, individualId, httpServletRequest);
    }

    /**
     *
     * @param
     * @return
     */
    @PutMapping("/individual/id/{individualId}/project/id/{projectId}/user/id/{userId}")
    public ResponseEntity<UserProjectPermissionDTO> updateUserIndividualPermissions(@PathVariable String projectId, @PathVariable String individualId, @RequestBody UserProjectPermissionDTO userProjectPermissionDTO, HttpServletRequest httpServletRequest) {
        return individualService.updateUserIndividualPermissions(projectId, individualId, userProjectPermissionDTO, httpServletRequest);
    }
}
