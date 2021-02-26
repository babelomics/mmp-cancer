package com.fujitsu.mmp.msusermanagement.controllers;


import com.fujitsu.mmp.msusermanagement.dto.user.UserDTO;
import com.fujitsu.mmp.msusermanagement.dto.user.UserPermissionDTO;
import com.fujitsu.mmp.msusermanagement.dto.user.filters.FilterUserDTO;
import com.fujitsu.mmp.msusermanagement.services.AuthService;
import com.fujitsu.mmp.msusermanagement.services.UserService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600, exposedHeaders = "Authorization")
@RequestMapping("/api/users")
@RestController
public class UserController {

    @Autowired
    private  UserService userService;

    @Autowired
    AuthService authService;

    /**
     * Create a new User
     * @param userDTO userDTO to create
     * @return
     */
    @PostMapping("/user")
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        return userService.createUser(userDTO);
    }

    /**
     * Get an element from the list
     * @param identifier: username of the entity to retrieve
     * @return UserDTO found
     */
    @GetMapping("user/id/{identifier}")
    public ResponseEntity<UserDTO> findByIdentifier(@PathVariable String identifier) {
        return userService.findByIdentifier(identifier);
    }

    /**
     * List all the users
     * @return list of all the users entities found
     */
    @GetMapping("")
    public ResponseEntity<List<UserDTO>> getAllUsers(){
        return userService.findAll();
    }

    /**
     * List all users with pagination
     * @return list of all users entities found
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
    public ResponseEntity<Page<UserDTO>> findAllByPage(@ApiIgnore("Ignored because swagger ui shows the wrong params, instead they are explained in the implicit params") Pageable pageable, FilterUserDTO filterUserDTO) {
        return userService.findAllByPage(pageable, filterUserDTO);
    }

    /**
     * Update an user entity
     * @param identifier of the user entity
     * @param userDTO entity to update
     * @return userDTO updated
     */
    @PutMapping("/user/id/{identifier}")
    public ResponseEntity<UserDTO> update(@PathVariable String identifier, @RequestBody UserDTO userDTO) {
        return userService.update(identifier, userDTO);
    }

    /**
     * Update the password of a user
     * @param identifier of the user entity
     * @param password entity to update
     * @return userDTO updated
     */
    @PutMapping("/user/id/{identifier}/changePassword")
    public ResponseEntity<UserDTO> changePassword (@PathVariable String identifier, @RequestBody String password) {
        return userService.changePassword(identifier, password);
    }

    /**
     * Delete a user entity
     * @param identifier identifier of the hello world entity
     * @return SuccessResponseDto with true if the element has been deleted
     */
    @DeleteMapping("/user/id/{identifier}")
    public ResponseEntity<?> delete(@PathVariable String identifier) {
        return userService.delete(identifier);
    }

    /**
     * Refresh user token
     * @return Refreshed token in Authorization header.
     */
    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        return authService.refreshToken(request.getHeader("Authorization").substring(7), response);
    }

    /**
     * Get a list of user permissions if the user is not an admin.
     * @return Object with the type of user and with the list of permissions if the user is not an admin.
     */
    @GetMapping("/user/permissions")
    public ResponseEntity<UserPermissionDTO> getPermissions (HttpServletRequest request) {
        return userService.getPermissions(request);
    }
}
