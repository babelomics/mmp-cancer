package com.fujitsu.mmp.msusermanagement.services;

import com.fujitsu.mmp.msusermanagement.dto.group.GroupDTO;
import com.fujitsu.mmp.msusermanagement.dto.group.OptionToDeleteDTO;
import com.fujitsu.mmp.msusermanagement.dto.group.PermissionDTO;
import com.fujitsu.mmp.msusermanagement.dto.group.filters.FilterUsersGroupsDTO;
import com.fujitsu.mmp.msusermanagement.entities.Group;
import com.fujitsu.mmp.msusermanagement.entities.Permission;
import com.fujitsu.mmp.msusermanagement.mappers.GroupMapper;
import com.fujitsu.mmp.msusermanagement.repositories.GroupRepository;
import com.fujitsu.mmp.msusermanagement.repositories.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {
    @Autowired
    GroupMapper groupMapper;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    PermissionRepository permissionRepository;

    public ResponseEntity<Page<GroupDTO>> listGroups(Pageable pageable, FilterUsersGroupsDTO filterUsersGroupsDTO) {
        HttpStatus responseStatus = HttpStatus.OK;
        Page<GroupDTO> responseBody;

        Page<Group> pageEntity = groupRepository.findUsersGroupsByFilters
                (filterUsersGroupsDTO, pageable);

        List<GroupDTO> groupDTOList = groupMapper.listEntityToListDto(pageEntity.getContent());
        groupDTOList.forEach(temp -> temp.setPermissionsNameList(temp.getPermissions()
                .stream()
                .map(aux -> aux.getAction() + " - " + aux.getEntityType()).collect(Collectors.toList())));

        responseBody = new PageImpl<>(groupDTOList, pageable, pageEntity.getTotalElements());

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<GroupDTO> createGroup(GroupDTO groupDTO) {

        HttpStatus responseStatus = HttpStatus.CREATED;
        GroupDTO responseBody = null;

        if (groupDTO.getGroupId() == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else if (groupRepository.existsByName(groupDTO.getName())) {
            responseStatus = HttpStatus.CONFLICT;
        } else {
            groupDTO.getUsers().forEach(temp -> {
                groupDTO.getPermissions().forEach(aux -> {
                    if (!permissionRepository.existsByActionAndEntityTypeAndEntityIdAndUserId(aux.getAction(), aux.getEntityType(), aux.getEntityId(), temp)) {
                        Permission permission = new Permission();
                        permission.setAction(aux.getAction());
                        permission.setEntityType(aux.getEntityType());
                        permission.setEntityId(aux.getEntityId());
                        permission.setUserId(temp);
                        permissionRepository.save(permission);
                    }
                });
            });

            Group usersGroup = groupMapper.dtoToEntity(groupDTO);
            Group createdUsersGroup = groupRepository.save(usersGroup);
            responseBody = groupMapper.entityToDto(createdUsersGroup);
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<GroupDTO> getGroup(String groupId) {
        HttpStatus responseStatus = HttpStatus.OK;
        GroupDTO responseBody = null;

        Group entity = groupRepository.findByGroupId(groupId);

        if (entity == null) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            responseBody = groupMapper.entityToDto(entity);
            responseBody.getPermissions().forEach(temp -> temp.setName(temp.getAction() + " " + temp.getEntityType()));

        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<Void> deleteGroup(String groupId, OptionToDeleteDTO optionToDeleteDTO) {
        HttpStatus responseStatus = HttpStatus.NO_CONTENT;
        Group group = groupRepository.findByGroupId(groupId);

        if (group == null) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            List<Permission> permissionList = new ArrayList<>();
            if(optionToDeleteDTO.getOption().equals("op1")){
                for (String userId: group.getUsers()) {
                    if(groupRepository.findByUsersAndProjectId(userId,group.getProjectId()).size()<=1){
                        permissionList = permissionRepository.findByUserIdAndEntityId(userId, group.getProjectId());
                    }
                }
            }

            if(optionToDeleteDTO.getOption().equals("op1") || optionToDeleteDTO.getOption().equals("op3"))
                for (String temp: group.getUsers()) {
                    for (PermissionDTO aux: group.getPermissions()) {
                        if (!groupRepository.hasPermissionsInAnotherGroup(temp, groupId, aux)) {
                            Permission permission = permissionRepository.findByUserIdAndActionAndEntityTypeAndEntityId(temp, aux.getAction(), aux.getEntityType(), aux.getEntityId());
                            permissionList.add(permission);
                        }
                    }
                }
            if (permissionList != null) {
                permissionRepository.deleteAll(permissionList);
            }
            groupRepository.delete(group);
        }
        
        return new ResponseEntity<Void>(responseStatus);
    }

    public ResponseEntity<GroupDTO> updateGroup(String groupId, GroupDTO groupDTO) {
        HttpStatus responseStatus = HttpStatus.OK;
        GroupDTO responseBody = null;

        if(groupId == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else {
            Group entity = groupRepository.findByGroupId(groupId);
            if (entity == null) {
                responseStatus = HttpStatus.NOT_FOUND;
            } else {
                Group entityToSave = groupMapper.dtoToEntity(groupDTO);
                groupDTO.getUsers().forEach(temp-> {
                    groupDTO.getPermissions().forEach(aux-> {
                        if(!permissionRepository.existsByActionAndEntityTypeAndEntityIdAndUserId(aux.getAction(),
                                aux.getEntityType(), aux.getEntityId(), temp)){
                            Permission permission = new Permission();
                            permission.setAction(aux.getAction());
                            permission.setEntityType(aux.getEntityType());
                            permission.setEntityId(aux.getEntityId());
                            permission.setUserId(temp);
                            permissionRepository.save(permission);
                        }else{
                            if(!groupRepository.hasPermissionsInAnotherGroup(temp, groupId, aux)){
                                Permission permission = permissionRepository.findByUserIdAndActionAndEntityTypeAndEntityId(temp, aux.getAction(), aux.getEntityType(), aux.getEntityId());
                                permissionRepository.delete(permission);
                            }
                        }
                    });
                });

                entityToSave = groupRepository.save(entityToSave);
                responseBody = groupMapper.entityToDto(entityToSave);
            }
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

}
