package com.fujitsu.mmp.msusermanagement.controllers;

import com.fujitsu.mmp.msusermanagement.dto.UserRegistryRequestDTO;
import com.fujitsu.mmp.msusermanagement.services.UserRegistryRequestService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/registry")
@RestController
public class UserRegistryRequestController {

    @Autowired
    private UserRegistryRequestService userRegistryRequestService;

    /**
     * Request to access the system. Create UserRegistryRequest entity.
     * @param userRegistryRequestDTO
     * @return
     */
    @PostMapping("/public/user")
    public ResponseEntity<?> createUserRegistryRequest(@RequestBody UserRegistryRequestDTO userRegistryRequestDTO) {
        return userRegistryRequestService.createUserRegistryRequest(userRegistryRequestDTO);
    }

    /**
     * List all the user registry requests entity in the bbdd
     * @return list of all the the user registry requests entities found
     */
    @GetMapping("")
    public ResponseEntity<List<UserRegistryRequestDTO>> getAllUsers(){
        return userRegistryRequestService.findAll();
    }

    /**
     * List all HelloWorld entity in the bbdd wit pagination
     * @return list of all HelloWorld entities found
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
    public ResponseEntity<Page<UserRegistryRequestDTO>> findAllByPage(@ApiIgnore("Ignored because swagger ui shows the wrong params, instead they are explained in the implicit params") Pageable pageable, UserRegistryRequestDTO userRegistryRequestDTO) {
        return userRegistryRequestService.findAllByPage(pageable, userRegistryRequestDTO);
    }

    /**
     * Update an HelloWorld entity
     * @param identifier of the user entity
     * @param userRegistryRequestDTO entity to update
     * @return userRegistryRequestDTO updated
     */
    @PutMapping("/id/{identifier}")
    public ResponseEntity<UserRegistryRequestDTO> update(@PathVariable String identifier, @RequestBody UserRegistryRequestDTO userRegistryRequestDTO) {
        return userRegistryRequestService.update(identifier, userRegistryRequestDTO);
    }

    /**
     * Get an element from the list
     * @param identifier: username of the entity to retrieve
     * @return UserDTO found
     */
    @GetMapping("/id/{identifier}")
    public ResponseEntity<UserRegistryRequestDTO> findByIdentifier(@PathVariable String identifier) {
        return userRegistryRequestService.findByIdentifier(identifier);
    }

}
