package com.fujitsu.mmp.msusermanagement.services;

import com.fujitsu.mmp.msusermanagement.constants.EPermissionAction;
import com.fujitsu.mmp.msusermanagement.constants.EPermissionEntityType;
import com.fujitsu.mmp.msusermanagement.constants.UserConstants;
import com.fujitsu.mmp.msusermanagement.dto.group.filters.FilterUsersPermissionsDTO;
import com.fujitsu.mmp.msusermanagement.dto.permission.PermissionDTO;
import com.fujitsu.mmp.msusermanagement.dto.permission.UserGroupPermissionDTO;
import com.fujitsu.mmp.msusermanagement.dto.permission.UserProjectPermissionDTO;
import com.fujitsu.mmp.msusermanagement.entities.Group;
import com.fujitsu.mmp.msusermanagement.entities.Permission;
import com.fujitsu.mmp.msusermanagement.entities.Project;
import com.fujitsu.mmp.msusermanagement.entities.User;
import com.fujitsu.mmp.msusermanagement.mappers.GroupMapper;
import com.fujitsu.mmp.msusermanagement.mappers.PermissionMapper;
import com.fujitsu.mmp.msusermanagement.repositories.GroupRepository;
import com.fujitsu.mmp.msusermanagement.repositories.PermissionRepository;
import com.fujitsu.mmp.msusermanagement.repositories.ProjectRepository;
import com.fujitsu.mmp.msusermanagement.repositories.UserRepository;
import com.fujitsu.mmp.msusermanagement.utility.JWTUtility;
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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PermissionService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    PermissionMapper permissionMapper;

    @Autowired
    GroupMapper groupMapper;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    JWTUtility jwtUtility;

    @Autowired
    PermissionService permissionService;

    public ResponseEntity<Page<UserProjectPermissionDTO>> listUsersPermissions(Pageable pageable, FilterUsersPermissionsDTO filterUsersPermissionsDTO, String projectId, HttpServletRequest httpServletRequest) {
        HttpStatus responseStatus = HttpStatus.OK;
        Page<UserProjectPermissionDTO> responseBody = null;

        if (projectId == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else if (!checkUpdatePermission(httpServletRequest, projectId)) {
            responseStatus = HttpStatus.FORBIDDEN;
        } else {

            Page<UserProjectPermissionDTO> userPage = userRepository.findUsersPermissionsByFiltersAndProjectId
                    (filterUsersPermissionsDTO, pageable, projectId);

            List<UserProjectPermissionDTO> userList = userPage.getContent();

            userList.forEach(temp -> {
                temp.getGroups().removeIf(group -> !projectId.equals(group.getProjectId()));
                temp.getGroups().removeIf(group -> !group.getUsers().contains(temp.getUserId()));
                temp.setGroupsIdList(temp.getGroups().stream().map(group -> group.getGroupId() + " ").collect(Collectors.toList()));
                temp.getPermissions().removeIf(permission -> !projectId.equals(permission.getEntityId()));
                temp.setPermissionsNameList(temp.getPermissions().stream().map(permission -> permission.getAction() + " " + permission.getEntityType()).collect(Collectors.toList()));
            });

            responseBody = new PageImpl<>(userList, pageable, userPage.getTotalElements());
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<Void> deleteUserFromProject(String userId, String projectId, HttpServletRequest httpServletRequest) {
        HttpStatus responseStatus = HttpStatus.NO_CONTENT;

        if (userId == null || projectId == null) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else if (!checkUpdatePermission(httpServletRequest, projectId)) {
            responseStatus = HttpStatus.FORBIDDEN;
        } else {
            List<Group> groupList = groupRepository.findByUsersAndProjectId(userId, projectId);
            List<Permission> permissionList = permissionRepository.findByUserIdAndEntityId(userId, projectId);

            for (Group group : groupList) {
                group.getUsers().removeIf(user -> user.equals(userId));
            }
            groupRepository.saveAll(groupList);
            permissionRepository.deleteAll(permissionList);
        }
        return new ResponseEntity<>(responseStatus);
    }

    public ResponseEntity<UserProjectPermissionDTO> addPermissionsToUser(String projectId, UserProjectPermissionDTO userProjectPermissionDTO) {
        HttpStatus responseStatus = HttpStatus.CREATED;

        if (userProjectPermissionDTO.getUserId() == null || userProjectPermissionDTO.getPermissions() == null || projectId == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else {
            Project project = projectRepository.findByProjectId(projectId);

            List<Permission> specificPermissionList =
                    permissionRepository.findByUserIdAndEntityTypeAndEntityIdIn(userProjectPermissionDTO.getUserId(), EPermissionEntityType.INDIVIDUAL.getValue(), project.getIndividuals());

            List<Permission> newPermissionList = userProjectPermissionDTO.getPermissions();

            if (!userProjectPermissionDTO.getGroupGuidList().isEmpty()) {
                Iterable<Group> groupList = groupRepository.findAllById(userProjectPermissionDTO.getGroupGuidList());
                addUserToGroups(groupList, userProjectPermissionDTO.getUserId());
            }

            specificPermissionList.forEach(specificPermission -> {
                if(newPermissionList.stream().anyMatch(permission -> specificPermission.getAction().equals(permission.getAction()) &&
                        (permission.getEntityType().equals(EPermissionEntityType.INDIVIDUALS.getValue()) ||
                                permission.getEntityType().equals(EPermissionEntityType.PROJECTS.getValue()))
                )){
                    permissionRepository.delete(specificPermission);
                }
            });

            addPermissionToUser(newPermissionList, userProjectPermissionDTO.getUserId());
        }
        return new ResponseEntity<>(responseStatus);
    }

    public ResponseEntity<UserGroupPermissionDTO> getUserPermissions(String userId, String projectId, HttpServletRequest httpServletRequest) {
        HttpStatus responseStatus = HttpStatus.OK;
        UserGroupPermissionDTO responseBody = null;

        if (projectId == null || projectId.isEmpty() || userId == null || userId.isEmpty()) {
            responseStatus = HttpStatus.BAD_REQUEST;
        } else if (!checkUpdatePermission(httpServletRequest, projectId)) {
            responseStatus = HttpStatus.FORBIDDEN;
        } else {
            User entity = userRepository.findByIdentifier(userId);

            if (entity == null) {
                responseStatus = HttpStatus.NOT_FOUND;
            } else {
                List<Permission> permissionList = permissionRepository.findByUserIdAndEntityId(userId, projectId);
                List<Group> groupList = groupRepository.findByUsersAndProjectId(userId, projectId);

                responseBody = new UserGroupPermissionDTO();
                responseBody.setUserId(userId);
                responseBody.setUserName(entity.getFirstName() + " " + entity.getLastName());
                responseBody.setPermissions(permissionMapper.listEntityToListDto(permissionList));
                responseBody.setGroupPermissions(groupMapper.listEntityToListDto(groupList));
            }
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<Page<UserProjectPermissionDTO>> listSpecificUsersPermissions(Pageable pageable, FilterUsersPermissionsDTO filterUsersPermissionsDTO, String projectId, HttpServletRequest httpServletRequest) {
        HttpStatus responseStatus = HttpStatus.OK;
        Page<UserProjectPermissionDTO> responseBody = null;

        if (projectId == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;

        } else if (!checkUpdatePermission(httpServletRequest, projectId)) {
            responseStatus = HttpStatus.FORBIDDEN;
        } else {
            Project project = projectRepository.findByProjectId(projectId);

            if (project == null) {
                responseStatus = HttpStatus.NOT_FOUND;
            } else {
                Page<UserProjectPermissionDTO> userPage = userRepository.findUsersSpecificPermissionsByFiltersAndProject
                        (filterUsersPermissionsDTO, pageable, project);

                List<UserProjectPermissionDTO> userList = userPage.getContent();

                userList.forEach(temp -> {
                    temp.getPermissions().removeIf(permission -> !project.getIndividuals().contains((permission.getEntityId()))
                            && !project.getSamples().contains((permission.getEntityId()))
                            && !project.getDrugs().contains((permission.getEntityId()))
                            && !project.getDiagnosticPanels().contains((permission.getEntityId()))
                            && !project.getAnalyses().contains((permission.getEntityId()))
                    );
                    temp.setPermissionsNameList(temp.getPermissions().stream().map(permission -> permission.getAction() + " " + permission.getEntityType() + " " + permission.getEntityId()).collect(Collectors.toList()));
                });

                responseBody = new PageImpl<>(userList, pageable, userPage.getTotalElements());
            }
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<UserProjectPermissionDTO> updateUserProjectPermissionDTO(String projectId, UserProjectPermissionDTO userProjectPermissionDTO, HttpServletRequest httpServletRequest) {
        HttpStatus responseStatus = HttpStatus.OK;
        UserProjectPermissionDTO responseBody = null;

        if (projectId == null || userProjectPermissionDTO.getUserId() == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else if (!checkUpdatePermission(httpServletRequest, projectId)) {
            responseStatus = HttpStatus.FORBIDDEN;
        } else {
            List<String> idGroupsDBList =
                    groupRepository.findByUsersAndProjectId(userProjectPermissionDTO.getUserId(), projectId).stream().map(Group::getGuid).collect(Collectors.toList());

            List<String> deletedIdsGroup = idGroupsDBList.stream()
                    .filter(temp -> !userProjectPermissionDTO.getGroupGuidList().contains(temp))
                    .collect(Collectors.toList());

            List<String> newIdsGroup = userProjectPermissionDTO.getGroupGuidList().stream()
                    .filter(temp -> !idGroupsDBList.contains(temp))
                    .collect(Collectors.toList());

            Iterable<Group> deletedGroups = groupRepository.findAllById(deletedIdsGroup);
            Iterable<Group> newGroups = groupRepository.findAllById(newIdsGroup);

            deleteUserFromGroups(deletedGroups, userProjectPermissionDTO.getUserId());
            addUserToGroups(newGroups, userProjectPermissionDTO.getUserId());

            List<Permission> permissionDBList =
                    permissionRepository.findByUserIdAndEntityId(userProjectPermissionDTO.getUserId(), projectId);

            List<Permission> deletedPermissions = permissionDBList.stream()
                    .filter(temp -> !userProjectPermissionDTO.getPermissions().contains(temp)).collect(Collectors.toList());

            List<Permission> newPermissions = userProjectPermissionDTO.getPermissions().stream()
                    .filter(temp -> !permissionDBList.contains(temp)).collect(Collectors.toList());

            permissionRepository.deleteAll(deletedPermissions);

            newPermissions.forEach(permission -> {
                if (!permissionRepository.existsByActionAndEntityTypeAndEntityIdAndUserId(permission.getAction(), permission.getEntityType(), permission.getEntityId(),
                        userProjectPermissionDTO.getUserId())) {
                    addPermissionToUser(userProjectPermissionDTO.getPermissions(), userProjectPermissionDTO.getUserId());
                }
            });
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    private void addUserToGroups(Iterable<Group> newGroups, String userId) {
        newGroups.forEach(group -> {
            if (!group.getUsers().contains(userId)) {
                List<String> userList = group.getUsers();
                userList.add(userId);
                group.setUsers(userList);
            }
        });
        groupRepository.saveAll(newGroups);
    }

    private void deleteUserFromGroups(Iterable<Group> deletedGroups, String userId) {
        deletedGroups.forEach(group -> group.getUsers().removeIf(user -> user.equals(userId)));
        groupRepository.saveAll(deletedGroups);
    }

    public void addPermissionToUser(List<Permission> permissionList, String userId) {
        permissionList.forEach(aux -> {
            if (!permissionRepository.existsByActionAndEntityTypeAndEntityIdAndUserId(aux.getAction(), aux.getEntityType(), aux.getEntityId(), userId)) {
                Permission permission = new Permission(aux.getAction(), aux.getEntityType(), aux.getEntityId(), userId);
                permissionRepository.save(permission);
            }
        });
    }

    public boolean hasAccess(HttpServletRequest httpServletRequest, PermissionDTO permission) {
        boolean hasAccess = false;

        String token = httpServletRequest.getHeader("Authorization");
        String username = "";

        if (token != null) {
            username = jwtUtility.getUsernameFromToken(token.substring(6));

            if (userRepository.findByIdentifier(username).getUserType().equals(UserConstants.USER_TYPE_ADMIN) ||
                    permissionRepository.existsByActionAndEntityTypeAndEntityIdAndUserId(permission.getAction(), permission.getEntityType(), permission.getEntityId(), username)) {
                hasAccess = true;
            }
        }
        return hasAccess;
    }

    public boolean checkUpdatePermission(HttpServletRequest httpServletRequest, String projectId) {

        PermissionDTO permission = new PermissionDTO(EPermissionAction.UPDATE.getValue(), EPermissionEntityType.PERMISSIONS.getValue(), projectId);

        return permissionService.hasAccess(httpServletRequest, permission);
    }
}
