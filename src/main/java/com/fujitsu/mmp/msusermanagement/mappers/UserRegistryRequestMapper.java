package com.fujitsu.mmp.msusermanagement.mappers;

import com.fujitsu.mmp.msusermanagement.dto.UserRegistryRequestDTO;
import com.fujitsu.mmp.msusermanagement.entities.UserRegistryRequest;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserRegistryRequestMapper {

    private final ModelMapper modelMapper;

    public UserRegistryRequestMapper() {
        this.modelMapper = new ModelMapper();
    }

    public UserRegistryRequest dtoToEntity(UserRegistryRequestDTO dto) {
        return modelMapper.map(dto, UserRegistryRequest.class);
    }

    public UserRegistryRequestDTO entityToDTO(UserRegistryRequest entity) {return modelMapper.map(entity, UserRegistryRequestDTO.class);}

    public List<UserRegistryRequest> listDTOToListEntity(List<UserRegistryRequestDTO> userRegistryRequestDTO){
        return userRegistryRequestDTO.stream().map(this::dtoToEntity).collect(Collectors.toList());
    }

    public List<UserRegistryRequestDTO> listEntityToListDto(List<UserRegistryRequest> listEntity) {
        return listEntity.stream().map(this::entityToDTO).collect(Collectors.toList());
    }

}
