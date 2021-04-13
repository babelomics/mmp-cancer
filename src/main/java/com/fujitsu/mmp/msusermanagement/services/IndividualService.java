package com.fujitsu.mmp.msusermanagement.services;

import com.fujitsu.mmp.msusermanagement.constants.EPermissionAction;
import com.fujitsu.mmp.msusermanagement.constants.EPermissionEntityType;
import com.fujitsu.mmp.msusermanagement.dto.group.filters.FilterUsersPermissionsDTO;
import com.fujitsu.mmp.msusermanagement.dto.individual.IndividualDTO;
import com.fujitsu.mmp.msusermanagement.dto.individual.IndividualPermissionDTO;
import com.fujitsu.mmp.msusermanagement.dto.individual.filters.FilterIndividualDTO;
import com.fujitsu.mmp.msusermanagement.dto.permission.PermissionDTO;
import com.fujitsu.mmp.msusermanagement.dto.permission.UserGroupPermissionDTO;
import com.fujitsu.mmp.msusermanagement.dto.permission.UserProjectPermissionDTO;
import com.fujitsu.mmp.msusermanagement.dto.project.ProjectDTO;
import com.fujitsu.mmp.msusermanagement.entities.*;
import com.fujitsu.mmp.msusermanagement.mappers.IndividualMapper;
import com.fujitsu.mmp.msusermanagement.mappers.PermissionMapper;
import com.fujitsu.mmp.msusermanagement.mappers.ProjectMapper;
import com.fujitsu.mmp.msusermanagement.repositories.IndividualRepository;
import com.fujitsu.mmp.msusermanagement.repositories.PermissionRepository;
import com.fujitsu.mmp.msusermanagement.repositories.ProjectRepository;
import com.fujitsu.mmp.msusermanagement.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IndividualService {

    @Autowired
    IndividualRepository individualRepository;

    @Autowired
    IndividualMapper individualMapper;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    UserService userService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    GenomicDictionaryService genomicDictionaryService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    PermissionMapper permissionMapper;

    public ResponseEntity<Page<IndividualDTO>> listIndividuals(Pageable pageable, FilterIndividualDTO filterIndividualDTO, HttpServletRequest httpServletRequest, String projectId) {
        HttpStatus responseStatus = HttpStatus.OK;
        Page<IndividualDTO> responseBody = null;

        if(projectId == null){
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        }else{
            Page<Individual> pageEntity =
                    individualRepository.findIndividualsByFilters(filterIndividualDTO, pageable, userService.getPermissions(httpServletRequest).getBody(), projectId);

            List<IndividualDTO> individualDTOList = individualMapper.listEntityToListDto(pageEntity.getContent());

            responseBody = new PageImpl<>(individualDTOList, pageable, pageEntity.getTotalElements());

        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<IndividualDTO> createIndividual(IndividualDTO individualDTO, String projectId, HttpServletRequest httpServletRequest) {
        HttpStatus responseStatus = HttpStatus.CREATED;
        IndividualDTO responseBody = null;

        if (projectId == null || individualDTO.getIndividualId() == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else {
            PermissionDTO createIndividuals = new PermissionDTO(EPermissionAction.CREATE.getValue(), EPermissionEntityType.INDIVIDUALS.getValue(), projectId);

            if (individualRepository.existsByIndividualId(individualDTO.getIndividualId())) {
                responseStatus = HttpStatus.CONFLICT;
            } else if (!permissionService.hasAccess(httpServletRequest, createIndividuals)){
                responseStatus = HttpStatus.FORBIDDEN;
            } else {
                Individual individual = individualMapper.dtoToEntity(individualDTO);

                individualRepository.save(individual);

                Project project = projectRepository.findByProjectId(projectId);
                project.getIndividuals().add(individual.getIndividualId());
                projectRepository.save(project);

                responseBody = individualMapper.entityToDto(individual);
            }
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<IndividualDTO> getIndividual(String individualId, String projectId) {
        HttpStatus responseStatus = HttpStatus.OK;
        IndividualDTO responseBody = null;

        if(individualId.isBlank() || projectId.isBlank()){
            responseStatus = HttpStatus.BAD_REQUEST;
        }else{
            Individual entity = individualRepository.findByIndividualId(individualId);
            Project project = projectRepository.findByProjectId(projectId);

            if(entity == null){
                responseStatus = HttpStatus.NOT_FOUND;
            }else{
                responseBody = individualMapper.entityToDto(entity);
                responseBody.setIsHuman(genomicDictionaryService.isHuman(project.getAssembly()));
            }
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<IndividualDTO> updateIndividual (String individualId, String projectId, IndividualDTO individualDTO, HttpServletRequest httpServletRequest){
        HttpStatus responseStatus = HttpStatus.OK;
        IndividualDTO responseBody = null;

        if(individualDTO.getIndividualId() == null){
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        }else if(!individualId.equals(individualDTO.getIndividualId())){
            responseStatus = HttpStatus.BAD_REQUEST;
        }else{
            Individual entity = individualRepository.findByIndividualId(individualId);

            List<PermissionDTO> permissionList = new ArrayList<>();

            PermissionDTO updateIndividuals = new PermissionDTO(EPermissionAction.UPDATE.getValue(),EPermissionEntityType.INDIVIDUALS.getValue(), projectId);
            PermissionDTO updateProject = new PermissionDTO(EPermissionAction.UPDATE.getValue(), EPermissionEntityType.PROJECT.getValue(), projectId);
            PermissionDTO updateIndividual = new PermissionDTO(EPermissionAction.UPDATE.getValue(), EPermissionEntityType.INDIVIDUAL.getValue(), individualId);

            permissionList.add(updateIndividuals);
            permissionList.add(updateProject);
            permissionList.add(updateIndividual);

            if(entity == null){
                responseStatus = HttpStatus.NOT_FOUND;
            } else if (permissionList.stream().noneMatch(permission -> permissionService.hasAccess(httpServletRequest, permission))) {
                responseStatus = HttpStatus.FORBIDDEN;
            } else {
                Individual individualToSave = individualMapper.dtoToEntity(individualDTO);
                individualToSave.setGuid(entity.getGuid());
                individualToSave.setModificationDate(new Date());

                Individual newIndividual = individualRepository.save(individualToSave);

                responseBody = individualMapper.entityToDto(newIndividual);
            }
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<IndividualDTO> deleteIndividual(String individualId, String projectId, HttpServletRequest httpServletRequest) {
        HttpStatus responseStatus = HttpStatus.NO_CONTENT;

        Individual elementToDelete = individualRepository.findByIndividualId(individualId);
        Project project = projectRepository.findByProjectId(projectId);

        if (individualId == null || projectId == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else {
            List<PermissionDTO> permissionList = new ArrayList<>();
            PermissionDTO deleteProject = new PermissionDTO(EPermissionAction.DELETE.getValue(),EPermissionEntityType.PROJECT.getValue(), projectId);
            PermissionDTO deleteIndividuals = new PermissionDTO(EPermissionAction.DELETE.getValue(), EPermissionEntityType.INDIVIDUALS.getValue(), projectId);

            permissionList.add(deleteProject);
            permissionList.add(deleteIndividuals);

            if (permissionList.stream().noneMatch(permission -> permissionService.hasAccess(httpServletRequest, permission))) {
                responseStatus = HttpStatus.FORBIDDEN;
            } else if (elementToDelete == null) {
                responseStatus = HttpStatus.NOT_FOUND;
            } else {
                project.getIndividuals().removeIf(temp -> temp.contains(individualId));
                projectRepository.save(project);

                individualRepository.delete(elementToDelete);
            }
        }
        return new ResponseEntity<>(responseStatus);
    }

    public ResponseEntity<Page<UserProjectPermissionDTO>> listIndividualsPermissions(Pageable pageable, FilterUsersPermissionsDTO filterUsersPermissionsDTO, HttpServletRequest httpServletRequest, String projectId, String individualId) {
        HttpStatus responseStatus = HttpStatus.OK;
        Page<UserProjectPermissionDTO> responseBody = null;

        if (projectId == null || individualId == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;

        } else if (!permissionService.checkUpdatePermission(httpServletRequest, projectId)) {
            responseStatus = HttpStatus.FORBIDDEN;
        } else {
            Individual individual = individualRepository.findByIndividualId(individualId);

            if (individual == null) {
                responseStatus = HttpStatus.NOT_FOUND;
            } else {
                Page<UserProjectPermissionDTO> userPage = userRepository.findUsersIndividualPermissions
                        (filterUsersPermissionsDTO, pageable, individualId);

                List<UserProjectPermissionDTO> userList = userPage.getContent();

                userList.forEach(temp -> {
                    temp.getPermissions().removeIf(permission -> !individualId.equals((permission.getEntityId())));
                    temp.setPermissionsNameList(temp.getPermissions().stream().map(permission -> permission.getAction() + " " + permission.getEntityType() + " " + permission.getEntityId()).collect(Collectors.toList()));
                });

                responseBody = new PageImpl<>(userList, pageable, userPage.getTotalElements());
            }
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<UserProjectPermissionDTO> addIndividualPermissionsToUser(IndividualPermissionDTO individualPermissionDTO, String projectId, HttpServletRequest httpServletRequest) {
        HttpStatus responseStatus = HttpStatus.CREATED;

        if (individualPermissionDTO.getUserId() == null || individualPermissionDTO.getPermissions() == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else if (!permissionService.checkUpdatePermission(httpServletRequest, projectId)) {
            responseStatus = HttpStatus.FORBIDDEN;
        } else {
            permissionService.addPermissionToUser(individualPermissionDTO.getPermissions(), individualPermissionDTO.getUserId());
        }
        return new ResponseEntity<>(responseStatus);
    }

    public ResponseEntity<Void> deleteIndividualPermissionsFromUser(String userId, String individualId, String projectId, HttpServletRequest httpServletRequest) {
        HttpStatus responseStatus = HttpStatus.NO_CONTENT;

        if (userId == null || individualId == null) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else if (!permissionService.checkUpdatePermission(httpServletRequest, projectId)) {
            responseStatus = HttpStatus.FORBIDDEN;
        } else {
            List<Permission> permissionList = permissionRepository.findByUserIdAndEntityId(userId, individualId);
            permissionRepository.deleteAll(permissionList);
        }
        return new ResponseEntity<>(responseStatus);
    }

    public ResponseEntity<UserGroupPermissionDTO> getUserIndividualPermissions(String userId, String projectId, String individualId, HttpServletRequest httpServletRequest) {
        HttpStatus responseStatus = HttpStatus.OK;
        UserGroupPermissionDTO responseBody = null;

        if (projectId == null || projectId.isEmpty() || userId == null || userId.isEmpty() || individualId == null || individualId.isEmpty()) {
            responseStatus = HttpStatus.BAD_REQUEST;
        } else if (!permissionService.checkUpdatePermission(httpServletRequest, projectId)) {
            responseStatus = HttpStatus.FORBIDDEN;
        } else {
            User entity = userRepository.findByIdentifier(userId);

            if (entity == null) {
                responseStatus = HttpStatus.NOT_FOUND;
            } else {
                List<Permission> permissionList = permissionRepository.findByUserIdAndEntityId(userId, individualId);

                responseBody = new UserGroupPermissionDTO();
                responseBody.setUserId(userId);
                responseBody.setUserName(entity.getFirstName() + " " + entity.getLastName());
                responseBody.setPermissions(permissionMapper.listEntityToListDto(permissionList));
            }
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<UserProjectPermissionDTO> updateUserIndividualPermissions(String projectId, String individualId, UserProjectPermissionDTO userProjectPermissionDTO, HttpServletRequest httpServletRequest) {
        HttpStatus responseStatus = HttpStatus.OK;
        UserProjectPermissionDTO responseBody = null;

        if (projectId == null || userProjectPermissionDTO.getUserId() == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else if (!permissionService.checkUpdatePermission(httpServletRequest, projectId)) {
            responseStatus = HttpStatus.FORBIDDEN;
        } else {
            List<Permission> permissionDBList =
                    permissionRepository.findByUserIdAndEntityId(userProjectPermissionDTO.getUserId(), individualId);

            List<Permission> deletedPermissions = permissionDBList.stream()
                    .filter(temp -> !userProjectPermissionDTO.getPermissions().contains(temp)).collect(Collectors.toList());

            List<Permission> newPermissions = userProjectPermissionDTO.getPermissions().stream()
                    .filter(temp -> !permissionDBList.contains(temp)).collect(Collectors.toList());

            permissionRepository.deleteAll(deletedPermissions);

            newPermissions.forEach(permission -> {
                if (!permissionRepository.existsByActionAndEntityTypeAndEntityIdAndUserId(permission.getAction(), permission.getEntityType(), permission.getEntityId(),
                        userProjectPermissionDTO.getUserId())) {
                    permissionService.addPermissionToUser(userProjectPermissionDTO.getPermissions(), userProjectPermissionDTO.getUserId());
                }
            });
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }
}
