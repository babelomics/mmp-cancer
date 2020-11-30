package com.fujitsu.mmp.msusermanagement.mappers;

import com.fujitsu.mmp.msusermanagement.dto.UserDTO;
import com.fujitsu.mmp.msusermanagement.dto.filters.FilterUserDTO;
import com.fujitsu.mmp.msusermanagement.entities.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    private final ModelMapper modelMapper;

    public UserMapper() {
        this.modelMapper = new ModelMapper();
    }

    public User dtoToEntity(UserDTO dto) {
        return modelMapper.map(dto, User.class);
    }

    public UserDTO entityToDTO(User entity) {return modelMapper.map(entity, UserDTO.class);}

    public FilterUserDTO filterEntityToDTO(User entity){return modelMapper.map(entity, FilterUserDTO.class);}

    public List<User> listDTOToListEntity(List<UserDTO> userDTO){
        return userDTO.stream().map(this::dtoToEntity).collect(Collectors.toList());
    }

    public List<UserDTO> listEntityToListDto(List<User> listEntity) {
        return listEntity.stream().map(this::entityToDTO).collect(Collectors.toList());
    }

    public List<FilterUserDTO> listFilterEntityToListDto(List<User> listEntity) {
        return listEntity.stream().map(this::filterEntityToDTO).collect(Collectors.toList());
    }

}
