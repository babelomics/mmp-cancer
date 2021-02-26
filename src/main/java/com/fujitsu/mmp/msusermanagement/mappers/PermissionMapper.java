package com.fujitsu.mmp.msusermanagement.mappers;

import com.fujitsu.mmp.msusermanagement.dto.group.PermissionDTO;
import com.fujitsu.mmp.msusermanagement.entities.Permission;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PermissionMapper {

    private final ModelMapper modelMapper;

    public PermissionMapper() {
        this.modelMapper = new ModelMapper();
    }

    public Permission dtoToEntity(PermissionDTO dto) {
        return modelMapper.map(dto, Permission.class);
    }

    public PermissionDTO entityToDTO(Permission entity) {return modelMapper.map(entity, PermissionDTO.class);}

    public List<Permission> listDTOToListEntity(List<PermissionDTO> permissionDTOList){
        return permissionDTOList.stream().map(this::dtoToEntity).collect(Collectors.toList());
    }

    public List<PermissionDTO> listEntityToListDto(List<Permission> listEntity) {
        return listEntity.stream().map(this::entityToDTO).collect(Collectors.toList());
    }

}
