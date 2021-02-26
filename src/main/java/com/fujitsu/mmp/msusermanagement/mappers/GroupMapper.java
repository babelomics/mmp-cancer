package com.fujitsu.mmp.msusermanagement.mappers;

import com.fujitsu.mmp.msusermanagement.dto.group.GroupDTO;
import com.fujitsu.mmp.msusermanagement.entities.Group;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GroupMapper {

    private final ModelMapper modelMapper;

    public GroupMapper() {
        modelMapper = new ModelMapper();
    }

    public Group dtoToEntity(GroupDTO dto){
        return modelMapper.map(dto, Group.class);
    }

    public GroupDTO entityToDto(Group entity) {
        return modelMapper.map(entity, GroupDTO.class);
    }

    public List<Group> listDtoToListEntity(List<GroupDTO> listDto) {
        return listDto.stream().map(this::dtoToEntity).collect(Collectors.toList());
    }

    public List<GroupDTO> listEntityToListDto(List<Group> listEntity) {
        return listEntity.stream().map(this::entityToDto).collect(Collectors.toList());
    }
}
