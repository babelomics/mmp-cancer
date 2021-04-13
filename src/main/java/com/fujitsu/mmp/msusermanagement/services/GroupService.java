package com.fujitsu.mmp.msusermanagement.services;

import com.fujitsu.mmp.msusermanagement.dto.group.GroupDTO;
import com.fujitsu.mmp.msusermanagement.dto.group.OptionDTO;
import com.fujitsu.mmp.msusermanagement.dto.permission.PermissionDTO;
import com.fujitsu.mmp.msusermanagement.dto.group.filters.FilterGroupsDTO;
import com.fujitsu.mmp.msusermanagement.entities.Group;
import com.fujitsu.mmp.msusermanagement.entities.Permission;
import com.fujitsu.mmp.msusermanagement.mappers.GroupMapper;
import com.fujitsu.mmp.msusermanagement.repositories.GroupRepository;
import com.fujitsu.mmp.msusermanagement.repositories.PermissionRepository;
import com.fujitsu.mmp.msusermanagement.repositories.UserRepository;
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

    @Autowired
    UserRepository userRepository;

    public ResponseEntity<Page<GroupDTO>> listGroups(Pageable pageable, FilterGroupsDTO filterGroupsDTO, String projectId) {
        HttpStatus responseStatus = HttpStatus.OK;
        Page<GroupDTO> responseBody = null;

        if(projectId == null){
            responseStatus= HttpStatus.UNPROCESSABLE_ENTITY;
        }else {

            Page<Group> pageEntity = groupRepository.findGroupsByFiltersAndProjectId
                    (filterGroupsDTO, pageable, projectId);

            List<GroupDTO> groupDTOList = groupMapper.listEntityToListDto(pageEntity.getContent());

            groupDTOList.stream().filter(temp -> temp.getPermissions() != null)
                    .forEach(temp -> temp.setPermissionsNameList(temp.getPermissions()
                    .stream()
                    .map(aux -> aux.getAction() + " - " + aux.getEntityType()).collect(Collectors.toList())));

            responseBody = new PageImpl<>(groupDTOList, pageable, pageEntity.getTotalElements());
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<GroupDTO> createGroup(GroupDTO groupDTO) {

        HttpStatus responseStatus = HttpStatus.CREATED;
        GroupDTO responseBody = null;

        if (groupDTO.getGroupId() == null || groupDTO.getGroupId().isEmpty() || groupDTO.getName() == null || groupDTO.getName().isEmpty() || groupDTO.getDescription() == null ||  groupDTO.getDescription().isEmpty()) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else if (groupRepository.existsByName(groupDTO.getName())) {
            responseStatus = HttpStatus.CONFLICT;
        } else {
            Group group = groupMapper.dtoToEntity(groupDTO);
            group.setUsers(new ArrayList<>());

            if (group.getPermissions() == null) {
                group.setPermissions(new ArrayList<>());
            }
            
            Group createdUsersGroup = groupRepository.save(group);

            responseBody = groupMapper.entityToDto(createdUsersGroup);
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<GroupDTO> getGroup(String groupGuid) {
        HttpStatus responseStatus = HttpStatus.OK;
        GroupDTO responseBody = null;

        Group entity = groupRepository.findByGuid(groupGuid);

        if (entity == null) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            responseBody = groupMapper.entityToDto(entity);
            
            if(responseBody.getPermissions() != null){
                responseBody.getPermissions().forEach(temp -> temp.setName(temp.getAction() + " " + temp.getEntityType()));
            }
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<Void> deleteGroup(String guid, OptionDTO optionDTO) {
        HttpStatus responseStatus = HttpStatus.NO_CONTENT;

        Group group = groupRepository.findByGuid(guid);

        if (group == null) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            List<Permission> permissionList = new ArrayList<>();
            if(optionDTO.getOption().equals("op1")){
                for (String userId: group.getUsers()) {
                    if(groupRepository.findByUsersAndProjectId(userId,group.getProjectId()).size()<=1){
                        permissionList = permissionRepository.findByUserIdAndEntityId(userId, group.getProjectId());
                    }
                }
            }

            if(optionDTO.getOption().equals("op1") || optionDTO.getOption().equals("op3"))
                for (String temp: group.getUsers()) {
                    for (PermissionDTO aux: group.getPermissions()) {
                        if (!groupRepository.hasPermissionsInAnotherGroup(temp, group.getName(), aux)) {
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
        
        return new ResponseEntity<>(responseStatus);
    }

    public ResponseEntity<GroupDTO> updateGroup(String groupGuid, GroupDTO groupDTO) {
        HttpStatus responseStatus = HttpStatus.OK;
        GroupDTO responseBody = null;

        if (groupDTO.getGroupId() == null || groupDTO.getGroupId().isEmpty() || groupDTO.getName() == null || groupDTO.getName().isEmpty() || groupDTO.getDescription() == null ||  groupDTO.getDescription().isEmpty()) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else if(!groupGuid.equals(groupDTO.getGuid())){
            responseStatus = HttpStatus.BAD_REQUEST;
        } else {
            Group entity = groupRepository.findByGuid(groupGuid);
            if (entity == null) {
                responseStatus = HttpStatus.NOT_FOUND;
            } else {
                Group entityToSave = groupMapper.dtoToEntity(groupDTO);

                if (entityToSave.getPermissions() == null) {
                    entityToSave.setPermissions(new ArrayList<>());
                }

                entityToSave.setGuid(entity.getGuid());

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
                            if(!groupRepository.hasPermissionsInAnotherGroup(temp, groupDTO.getName(), aux)){
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
