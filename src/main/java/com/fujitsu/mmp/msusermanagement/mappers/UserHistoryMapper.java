package com.fujitsu.mmp.msusermanagement.mappers;

import com.fujitsu.mmp.msusermanagement.dto.UserHistoryDTO;
import com.fujitsu.mmp.msusermanagement.entities.UserHistory;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserHistoryMapper {
    private final ModelMapper modelMapper;

    public UserHistoryMapper() {
        this.modelMapper = new ModelMapper();
    }

    public UserHistory dtoToEntity(UserHistoryDTO dto) {
        return modelMapper.map(dto, UserHistory.class);
    }

    public UserHistoryDTO entityToDTO(UserHistory entity) {return modelMapper.map(entity, UserHistoryDTO.class);}

    public List<UserHistory> listDTOToListEntity(List<UserHistoryDTO> userHistoryDTO){
        return userHistoryDTO.stream().map(this::dtoToEntity).collect(Collectors.toList());
    }

    public List<UserHistoryDTO> listEntityToListDto(List<UserHistory> listEntity) {
        return listEntity.stream().map(this::entityToDTO).collect(Collectors.toList());
    }
}
