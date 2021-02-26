package com.fujitsu.mmp.msusermanagement.controllers;

import com.fujitsu.mmp.msusermanagement.dto.group.GroupDTO;
import com.fujitsu.mmp.msusermanagement.dto.group.OptionToDeleteDTO;
import com.fujitsu.mmp.msusermanagement.dto.group.filters.FilterUsersGroupsDTO;
import com.fujitsu.mmp.msusermanagement.services.GroupService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@CrossOrigin(origins = "*", maxAge = 3600, exposedHeaders = "Authorization")
@RequestMapping("/api/groups")
@RestController
public class GroupController {
    @Autowired
    private GroupService groupService;

    /**
     * List all projects with pagination
     * @return list of all projects entities found
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

    public ResponseEntity<Page<GroupDTO>> listGroups(@ApiIgnore("Ignored because swagger ui shows the wrong params, instead they are explained in the implicit params") Pageable pageable, FilterUsersGroupsDTO filterUsersGroupsDTO) {
        return groupService.listGroups(pageable, filterUsersGroupsDTO);
    }

    /**
     * Create a new Group
     * @param groupDTO groupDTO to create
     * @return
     */
    @PostMapping("/group")

    public ResponseEntity<GroupDTO> createGroup(@RequestBody GroupDTO groupDTO) {
        return groupService.createGroup(groupDTO);
    }

    /**
     * Get details of groups.
     * @param groupId: identifier of the entity to retrieve.
     * @return
     */
    @GetMapping("/group/id/{groupId}")
    public ResponseEntity<GroupDTO> getGroup(@PathVariable String groupId){
        return groupService.getGroup(groupId);

    }

    /**
     * Update an group entity
     * @param groupId: identifier of the entity
     * @param groupDTO entity to update
     * @return groupDTO updated
     */
    @PutMapping("/group/id/{groupId}")
    public ResponseEntity<GroupDTO> updateGroup(@PathVariable String groupId, @RequestBody GroupDTO groupDTO) {
        return groupService.updateGroup(groupId, groupDTO);
    }

    /**
     * Delete a group.
     * @param groupId
     * @return
     */
    @DeleteMapping("/id/{groupId}/group")
    public ResponseEntity<Void> deleteGroup(@PathVariable String groupId, @RequestBody OptionToDeleteDTO optionToDeleteDTO){
        return groupService.deleteGroup(groupId, optionToDeleteDTO);
    }

}
