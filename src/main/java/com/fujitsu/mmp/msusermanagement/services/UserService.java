package com.fujitsu.mmp.msusermanagement.services;

import com.fujitsu.mmp.msusermanagement.dto.UserDTO;
import com.fujitsu.mmp.msusermanagement.dto.UserHistoryDTO;
import com.fujitsu.mmp.msusermanagement.email.EmailServiceImpl;
import com.fujitsu.mmp.msusermanagement.entities.Permission;
import com.fujitsu.mmp.msusermanagement.entities.User;
import com.fujitsu.mmp.msusermanagement.mappers.UserMapper;
import com.fujitsu.mmp.msusermanagement.model.response.MessageResponse;
import com.fujitsu.mmp.msusermanagement.repositories.PermissionRepository;
import com.fujitsu.mmp.msusermanagement.repositories.UserRepository;
import com.fujitsu.mmp.msusermanagement.utility.JWTUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserHistoryService userHistoryService;

    @Autowired
    private EmailServiceImpl emailService;

    @Autowired
    private JWTUtility jwtUtility;

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

        User createdUser = userRepository.save(user);

        if(userDTO.getCanCreateProject()) {
            Permission userPermissionCreateProjects
                    = findPermission("create-project-undefined");

            Set<User> users = userPermissionCreateProjects.getUsers();
            users.add(createdUser);

            userPermissionCreateProjects.setUsers(users);

            permissionRepository.save(userPermissionCreateProjects);
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


    public ResponseEntity<Page<UserDTO>> findAllByPage(Pageable pageable, UserDTO userDTO) {
        HttpStatus responseStatus = HttpStatus.OK;
        Page<UserDTO> responseBody;

        userDTO.setLastName(userDTO.getFirstName());
        User user = userMapper.dtoToEntity(userDTO);

        ExampleMatcher customExampleMatcher = ExampleMatcher.matching()
                .withMatcher("identifier", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("firstName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("lastName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("organization", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("email", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("userType", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());


        Example userExample = Example.of(user, customExampleMatcher);

        Page<User> pageEntity = userRepository.findAll(userExample, pageable);

        List<UserDTO> contentDto = userMapper.listEntityToListDto(pageEntity.getContent());
        responseBody = new PageImpl<>(contentDto, pageable, pageEntity.getTotalElements());

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
            } else if (!entity.getVersion().equals(userDTO.getVersion())) {
                responseStatus = HttpStatus.CONFLICT;
            } else {
                User entityToSave = userMapper.dtoToEntity(userDTO);
                entityToSave.setId(entity.getId());
                entityToSave = userRepository.save(entityToSave);
                responseBody = userMapper.entityToDTO(entityToSave);
            }
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<Void> delete(String identifier) {
        HttpStatus responseStatus = HttpStatus.NO_CONTENT;

        User elementToDelete = userRepository.findByIdentifier(identifier);

        if (elementToDelete == null) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {

            UserDTO userDTO = userMapper.entityToDTO(elementToDelete);
            UserHistoryDTO userHistoryDTO = new UserHistoryDTO();
            userHistoryDTO.setFirstName((userDTO.getFirstName()));
            userHistoryDTO.setLastName(userDTO.getLastName());
            userHistoryDTO.setPassword(userDTO.getPassword());
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

    Permission findPermission(String permissionName) {
        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new RuntimeException("Error: Permission is not found."));
        return permission;
    }

    public ResponseEntity<UserDTO> changePassword(String identifier, UserDTO userDTO) {
        HttpStatus responseStatus = HttpStatus.OK;
        UserDTO responseBody = null;

        if (identifier == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else {
            User entity = userRepository.findByIdentifier(identifier);
            if (entity == null) {
                responseStatus = HttpStatus.NOT_FOUND;
            } else {
                entity.setPassword(encoder.encode(userDTO.getPassword()));
                entity = userRepository.save(entity);
                responseBody = userMapper.entityToDTO(entity);
            }
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }
}
