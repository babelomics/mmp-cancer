package com.fujitsu.mmp.msusermanagement.services;

import com.fujitsu.mmp.msusermanagement.constants.*;
import com.fujitsu.mmp.msusermanagement.dto.jwt.response.MessageResponse;
import com.fujitsu.mmp.msusermanagement.dto.user.UserDTO;
import com.fujitsu.mmp.msusermanagement.dto.user.UserHistoryDTO;
import com.fujitsu.mmp.msusermanagement.dto.user.UserPermissionDTO;
import com.fujitsu.mmp.msusermanagement.dto.user.filters.FilterUserDTO;
import com.fujitsu.mmp.msusermanagement.email.EmailServiceImpl;
import com.fujitsu.mmp.msusermanagement.entities.Configuration;
import com.fujitsu.mmp.msusermanagement.entities.Permission;
import com.fujitsu.mmp.msusermanagement.entities.User;
import com.fujitsu.mmp.msusermanagement.mappers.PermissionMapper;
import com.fujitsu.mmp.msusermanagement.mappers.UserMapper;
import com.fujitsu.mmp.msusermanagement.repositories.ConfigurationRepository;
import com.fujitsu.mmp.msusermanagement.repositories.PermissionRepository;
import com.fujitsu.mmp.msusermanagement.repositories.UserRepository;
import com.fujitsu.mmp.msusermanagement.utility.JWTUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    ConfigurationRepository configurationRepository;

    @Autowired
    PermissionMapper permissionMapper;

    @Autowired
    UserHistoryService userHistoryService;

    @Autowired
    EmailServiceImpl emailService;

    @Autowired
    JWTUtility jwtUtility;

    @Autowired
    PasswordEncoder encoder;

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public ResponseEntity<?> createUser(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            return new ResponseEntity<>(
                    "Error: Email is already in use!",
                    HttpStatus.CONFLICT);
        }

        if (userRepository.existsByIdentifier(userDTO.getIdentifier())) {
            return new ResponseEntity<>(
                    "Error: Identifier is already taken!",
                    HttpStatus.CONFLICT);
        }

        User user = userMapper.dtoToEntity(userDTO);
        user.setDateCreated(new Date());
        user.setPassword(encoder.encode(UUID.randomUUID().toString()));

        userRepository.save(user);

        if(userDTO.getCanCreateProject()) {
            createProjectPermission(user.getIdentifier());
        }

        emailService.sendLinkToPasswordScreen(user.getIdentifier());

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    public ResponseEntity<UserDTO> findByIdentifier(String identifier) {
        HttpStatus responseStatus = HttpStatus.OK;
        UserDTO responseBody = null;

        User entity = userRepository.findByIdentifier(identifier);
        if (entity == null) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            responseBody = userMapper.entityToDTO(entity);
            responseBody.setCanCreateProject(permissionRepository.existsByUserIdAndActionAndEntityType(entity.getIdentifier(), "create", "projects"));
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<List<UserDTO>> findAll() {
        HttpStatus responseStatus = HttpStatus.OK;
        List<UserDTO> responseBody = null;

        List<User> listEntity = userRepository.findAll();
        if (listEntity.isEmpty()) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            responseBody = userMapper.listEntityToListDto(listEntity);
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<Page<UserDTO>> findAllByPage(Pageable pageable, FilterUserDTO filterUserDTO) {
        HttpStatus responseStatus = HttpStatus.OK;

        Page<UserDTO> responseBody;

        Page<User> pageEntity = userRepository.findUsersByFilters(filterUserDTO, pageable);

        List<UserDTO> userDTOList = userMapper.listEntityToListDto(pageEntity.getContent());

        responseBody = new PageImpl<>(userDTOList, pageable, pageEntity.getTotalElements());

        return new ResponseEntity<>(responseBody, responseStatus);
    }


    public ResponseEntity<UserDTO> update(String identifier, UserDTO userDTO) {
        HttpStatus responseStatus = HttpStatus.OK;
        UserDTO responseBody = null;

        if (userDTO.getIdentifier() == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else if (!identifier.equals(userDTO.getIdentifier())) {
            responseStatus = HttpStatus.BAD_REQUEST;
        } else {
            User entity = userRepository.findByIdentifier(identifier);
            if (entity == null) {
                responseStatus = HttpStatus.NOT_FOUND;
            } else if (entity.getUserType().equals(UserConstants.USER_TYPE_ADMIN) && !entity.getUserType().equals(userDTO.getUserType()) &&
                    userRepository.countByUserType(entity.getUserType()) <= 1) {
                responseStatus = HttpStatus.LOCKED;
            } else if (entity.getUserType().equals(UserConstants.USER_TYPE_ADMIN) && !entity.getUserType().equals(userDTO.getUserType()) &&
            configurationRepository.findByContactIdentifier(identifier) != null){
                responseStatus = HttpStatus.EXPECTATION_FAILED;
            } else if (!entity.getVersion().equals(userDTO.getVersion())) {
                responseStatus = HttpStatus.CONFLICT;
            }else if(!userDTO.getEmail().equals(entity.getEmail()) && userRepository.existsByEmail(userDTO.getEmail())){
                responseStatus = HttpStatus.CONFLICT;
            } else {

                User entityToSave = userMapper.dtoToEntity(userDTO);
                entityToSave.setId(entity.getId());
                entityToSave.setPassword(entity.getPassword());
                entityToSave = userRepository.save(entityToSave);
                responseBody = userMapper.entityToDTO(entityToSave);

                if(!userDTO.getCanCreateProject().equals(permissionRepository.existsByUserIdAndActionAndEntityType(entityToSave.getIdentifier(), "create", "projects"))) {

                    if (userDTO.getCanCreateProject()) {
                        createProjectPermission(entityToSave.getIdentifier());
                        responseBody.setCanCreateProject(true);
                    } else {
                        permissionRepository.delete(permissionRepository.findByUserIdAndActionAndEntityType(entityToSave.getIdentifier(), EPermissionAction.CREATE.getValue(), EPermissionEntityType.PROJECTS.getValue()));
                        permissionRepository.delete(permissionRepository.findByUserIdAndActionAndEntityType(entityToSave.getIdentifier(), EPermissionAction.READ.getValue(), EPermissionEntityType.PROJECTS.getValue()));
                        responseBody.setCanCreateProject(false);
                    }
                }

            }
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<?> delete(String identifier) {
        HttpStatus responseStatus = HttpStatus.NO_CONTENT;

        User elementToDelete = userRepository.findByIdentifier(identifier);

        if (elementToDelete == null) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            if(elementToDelete.getUserType().equals(UserConstants.USER_TYPE_ADMIN)){
                if(userRepository.countByUserType(elementToDelete.getUserType()) <= 1){
                    return new ResponseEntity<>(
                            "Error: It is necessary to have at least one administrator in the system!",
                            HttpStatus.LOCKED);
                }
                Configuration configuration = configurationRepository.findByContactIdentifier(identifier);
                if(configuration != null){
                    return new ResponseEntity<>(
                            "Error: You will proceed to delete the contact administrator user.",
                            HttpStatus.EXPECTATION_FAILED);
                }
            }

            UserDTO userDTO = userMapper.entityToDTO(elementToDelete);
            UserHistoryDTO userHistoryDTO = new UserHistoryDTO();
            userHistoryDTO.setFirstName((userDTO.getFirstName()));
            userHistoryDTO.setLastName(userDTO.getLastName());
            userHistoryDTO.setIdentifier(userDTO.getIdentifier());
            userHistoryDTO.setAccessType(userDTO.getAccessType());
            userHistoryDTO.setEmail(userDTO.getEmail());
            userHistoryDTO.setOrganization(userDTO.getOrganization());
            userHistoryDTO.setDateCreated(userDTO.getDateCreated());
            userHistoryDTO.setDateLastAccess(userDTO.getDateLastAccess());
            userHistoryDTO.setUserType(userDTO.getUserType());

            ResponseEntity<UserHistoryDTO> result = userHistoryService.createUserHistory(userHistoryDTO);

            if(!result.getStatusCode().equals(HttpStatus.CREATED)){
                responseStatus = result.getStatusCode();
            } else {
                userRepository.delete(elementToDelete);
            }
        }

        return new ResponseEntity<>(responseStatus);
    }

    public ResponseEntity<UserDTO> changePassword(String identifier, String password) {
        HttpStatus responseStatus = HttpStatus.OK;
        UserDTO responseBody = null;

        if (identifier == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else {
            User entity = userRepository.findByIdentifier(identifier);
            if (entity == null) {
                responseStatus = HttpStatus.NOT_FOUND;
            } else {
                entity.setPassword(encoder.encode(password));
                entity = userRepository.save(entity);
                responseBody = userMapper.entityToDTO(entity);
                responseBody.setCanCreateProject(permissionRepository.existsByUserIdAndActionAndEntityType(entity.getIdentifier(), "create", "projects"));
            }
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    private void createProjectPermission(String userId) {

        Permission createProjectPermission = new Permission(EPermissionAction.CREATE.getValue(),EPermissionEntityType.PROJECTS.getValue(), "undefined",userId);
        Permission readProjectPermission = new Permission(EPermissionAction.READ.getValue(),EPermissionEntityType.PROJECTS.getValue(),"undefined",userId);

        List<Permission> permissionList = new ArrayList<>();

        permissionList.add(createProjectPermission);
        permissionList.add(readProjectPermission);

        permissionRepository.saveAll(permissionList);
    }

    public ResponseEntity<UserPermissionDTO> getPermissions(HttpServletRequest request) {
        HttpStatus responseStatus = HttpStatus.OK;
        UserPermissionDTO responseBody = new UserPermissionDTO();
        List<Permission> permissionList;

        String token = request.getHeader("Authorization");
        String username = "";

        if (token != null) {
            username = jwtUtility.getUsernameFromToken(token.substring(6));
        }

        if (userRepository.findByIdentifier(username).getUserType().equals(UserConstants.USER_TYPE_ADMIN)) {
            responseBody.setUserType(UserConstants.USER_TYPE_ADMIN);
        } else {
            responseBody.setUserType(UserConstants.USER_TYPE_USER);
            permissionList = permissionRepository.findByUserId(username);
            responseBody.setPermissionList(permissionMapper.listEntityToListDto(permissionList));
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }
}
