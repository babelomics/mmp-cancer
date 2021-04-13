package com.fujitsu.mmp.msusermanagement.controllers;

import com.fujitsu.mmp.msusermanagement.dto.group.filters.FilterUsersPermissionsDTO;
import com.fujitsu.mmp.msusermanagement.dto.permission.UserGroupPermissionDTO;
import com.fujitsu.mmp.msusermanagement.dto.permission.UserProjectPermissionDTO;
import com.fujitsu.mmp.msusermanagement.services.PermissionService;
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
@RequestMapping("/api/permissions")
@RestController
public class PermissionController {

    @Autowired
    PermissionService permissionService;

    /**
     * List all user permissions for a given project.
     * @return list of all users for a project.
     */
    @GetMapping("/project/id/{projectId}/list")
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
    public ResponseEntity<Page<UserProjectPermissionDTO>> listUsersPermissions(@ApiIgnore("Ignored because swagger ui shows the wrong params, instead they are explained in the implicit params") Pageable pageable, FilterUsersPermissionsDTO filterUsersPermissionsDTO, @PathVariable String projectId, HttpServletRequest httpServletRequest) {
        return permissionService.listUsersPermissions(pageable, filterUsersPermissionsDTO, projectId, httpServletRequest);
    }

    /**
     * Get the user permission in a project.
     * @param userId
     * @param projectId
     */
    @GetMapping("/project/id/{projectId}/user/id/{userId}")
    public ResponseEntity<UserGroupPermissionDTO> getUserPermissions(@PathVariable String userId, @PathVariable String projectId, HttpServletRequest httpServletRequest){
        return permissionService.getUserPermissions(userId, projectId, httpServletRequest);
    }

    /**
     * Delete a user from a project.
     * @param userId
     * @return
     */
    @DeleteMapping("/project/id/{projectId}/remove/user/id/{userId}")
    public ResponseEntity<Void> deleteUserFromProject(@PathVariable String userId, @PathVariable String projectId, HttpServletRequest httpServletRequest){
        return permissionService.deleteUserFromProject(userId, projectId, httpServletRequest);
    }

    /**
     * Create a new UserProjectPermissionDTO
     * @param userProjectPermissionDTO userProjectPermissionDTO to create
     * @return
     */
    @PostMapping("/project/id/{projectId}/add/user/id/{userId}")
    public ResponseEntity<UserProjectPermissionDTO> addPermissionsToUser(@PathVariable String projectId, @RequestBody UserProjectPermissionDTO userProjectPermissionDTO) {
        return permissionService.addPermissionsToUser(projectId, userProjectPermissionDTO);
    }

    /**
     * List all user permissions for a given project.
     * @return list of all users for a project.
     */
    @GetMapping("/project/id/{projectId}/specificPermission/list")
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
    public ResponseEntity<Page<UserProjectPermissionDTO>> listSpecificUsersPermissions(@ApiIgnore("Ignored because swagger ui shows the wrong params, instead they are explained in the implicit params") Pageable pageable, FilterUsersPermissionsDTO filterUsersPermissionsDTO, @PathVariable String projectId, HttpServletRequest httpServletRequest) {
        return permissionService.listSpecificUsersPermissions(pageable, filterUsersPermissionsDTO, projectId, httpServletRequest);
    }

    /**
     * Update an userProjectPermissionDTO
     * @param userProjectPermissionDTO entity to update
     * @return userProjectPermissionDTO updated
     */
    @PutMapping("project/id/{projectId}/user/id/{userId}")
    public ResponseEntity<UserProjectPermissionDTO> updateUserProjectPermissionDTO(@PathVariable String projectId, @RequestBody UserProjectPermissionDTO userProjectPermissionDTO, HttpServletRequest httpServletRequest) {
        return permissionService.updateUserProjectPermissionDTO(projectId, userProjectPermissionDTO, httpServletRequest);
    }
}
