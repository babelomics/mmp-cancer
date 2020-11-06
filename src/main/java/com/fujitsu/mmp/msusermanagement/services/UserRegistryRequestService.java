package com.fujitsu.mmp.msusermanagement.services;

import com.fujitsu.mmp.msusermanagement.dto.NotificationDTO;
import com.fujitsu.mmp.msusermanagement.dto.UserDTO;
import com.fujitsu.mmp.msusermanagement.dto.UserRegistryRequestDTO;
import com.fujitsu.mmp.msusermanagement.email.EmailServiceImpl;
import com.fujitsu.mmp.msusermanagement.entities.User;
import com.fujitsu.mmp.msusermanagement.entities.UserRegistryRequest;
import com.fujitsu.mmp.msusermanagement.mappers.UserMapper;
import com.fujitsu.mmp.msusermanagement.mappers.UserRegistryRequestMapper;
import com.fujitsu.mmp.msusermanagement.repositories.UserRegistryRequestRepository;
import com.fujitsu.mmp.msusermanagement.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserRegistryRequestService {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailServiceImpl emailService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRepository userRepository;

    private final UserRegistryRequestRepository userRegistryRequestRepository;
    private final UserRegistryRequestMapper userRegistryRequestMapper;

    public UserRegistryRequestService (UserRegistryRequestRepository userRegistryRequestRepository, UserRegistryRequestMapper userRegistryRequestMapper){
        this.userRegistryRequestMapper = userRegistryRequestMapper;
        this.userRegistryRequestRepository = userRegistryRequestRepository;
    }

    public ResponseEntity<?> createUserRegistryRequest(UserRegistryRequestDTO userRegistryRequestDTO) {
        HttpStatus responseStatus = HttpStatus.CREATED;
        UserRegistryRequestDTO responseBody = null;

        if (userRegistryRequestRepository.existsByEmail(userRegistryRequestDTO.getEmail())) {
            return new ResponseEntity<>(
                    "Error: Email is already in use!",
                    HttpStatus.CONFLICT);
        } else if (userRegistryRequestRepository.existsByIdentifier(userRegistryRequestDTO.getIdentifier())) {
            return new ResponseEntity<>(
                    "Error: Identifier is already taken!",
                    HttpStatus.CONFLICT);
        }else if (userRepository.existsByEmail(userRegistryRequestDTO.getEmail())){
            return new ResponseEntity<>(
                    "Error: Email is already in use!",
                    HttpStatus.CONFLICT);
        }else if (userRepository.existsByIdentifier(userRegistryRequestDTO.getIdentifier())){
            return new ResponseEntity<>(
                    "Error: Identifier is already taken!",
                    HttpStatus.CONFLICT);
        } else {
            userRegistryRequestDTO.setApplicationDate(new Date());
            UserRegistryRequest entity = userRegistryRequestMapper.dtoToEntity(userRegistryRequestDTO);
            entity = userRegistryRequestRepository.save(entity);
            responseBody = userRegistryRequestMapper.entityToDTO(entity);

            notificationService.createNotification(new NotificationDTO(userRegistryRequestDTO.getVersion(), "UserRegistryRequest", userRegistryRequestDTO.getIdentifier()));
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<List<UserRegistryRequestDTO>> findAll() {
        HttpStatus responseStatus = HttpStatus.OK;
        List<UserRegistryRequestDTO> responseBody = null;

        List<UserRegistryRequest> listEntity = userRegistryRequestRepository.findAll();
        if (listEntity.isEmpty()) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            responseBody = userRegistryRequestMapper.listEntityToListDto(listEntity);
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<Page<UserRegistryRequestDTO>> findAllByPage(Pageable pageable, UserRegistryRequestDTO userRegistryRequestDTO) {
        HttpStatus responseStatus = HttpStatus.OK;
        Page<UserRegistryRequestDTO> responseBody;

        userRegistryRequestDTO.setLastName(userRegistryRequestDTO.getFirstName());
        UserRegistryRequest userRegistryRequest = userRegistryRequestMapper.dtoToEntity(userRegistryRequestDTO);

        ExampleMatcher customExampleMatcher = ExampleMatcher.matching()
                .withMatcher("identifier", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("firstName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("lastName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("organization", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());

        Example userRegistryRequestExample = Example.of(userRegistryRequest, customExampleMatcher);

        Page<UserRegistryRequest> pageEntity = userRegistryRequestRepository.findAll(userRegistryRequestExample, pageable);


        List<UserRegistryRequestDTO> contentDto = userRegistryRequestMapper.listEntityToListDto(pageEntity.getContent());
        responseBody = new PageImpl<>(contentDto, pageable, pageEntity.getTotalElements());

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<UserRegistryRequestDTO> update(String identifier, UserRegistryRequestDTO userRegistryRequestDTO) {

        HttpStatus responseStatus = HttpStatus.OK;
        UserRegistryRequestDTO responseBody = null;

        if(userRegistryRequestDTO.getIdentifier() == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else if (!identifier.equals(userRegistryRequestDTO.getIdentifier())){
            responseStatus = HttpStatus.BAD_REQUEST;
        } else {
            UserRegistryRequest entity = userRegistryRequestRepository.findByIdentifier(identifier);
            if(entity == null) {
                responseStatus = HttpStatus.NOT_FOUND;
            } else if (!entity.getVersion().equals(userRegistryRequestDTO.getVersion())) {
                responseStatus = HttpStatus.CONFLICT;
            } else {
                UserRegistryRequest entityToSave = userRegistryRequestMapper.dtoToEntity(userRegistryRequestDTO);
                entityToSave.setId(entity.getId());
                entityToSave = userRegistryRequestRepository.save(entityToSave);
                responseBody = userRegistryRequestMapper.entityToDTO(entityToSave);

                UserDTO userDTO = new UserDTO();
                userDTO.setFirstName(userRegistryRequestDTO.getFirstName());
                userDTO.setLastName(userRegistryRequestDTO.getLastName());
                userDTO.setUserType(userRegistryRequestDTO.getUserType());
                userDTO.setAccessType(userRegistryRequestDTO.getAccessType());
                userDTO.setOrganization(userRegistryRequestDTO.getOrganization());
                userDTO.setIdentifier(userRegistryRequestDTO.getIdentifier());
                userDTO.setEmail(userRegistryRequestDTO.getEmail());
                userDTO.setDateCreated(new Date());
                User user = userMapper.dtoToEntity(userDTO);

                userRepository.save(user);

                emailService.sendAccessRequestEmail(userRegistryRequestDTO);
            }
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<UserRegistryRequestDTO> findByIdentifier(String identifier) {
        HttpStatus responseStatus = HttpStatus.OK;
        UserRegistryRequestDTO responseBody = null;

        UserRegistryRequest entity = userRegistryRequestRepository.findByIdentifier(identifier);
        if (entity == null) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            responseBody = userRegistryRequestMapper.entityToDTO(entity);
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }
}
