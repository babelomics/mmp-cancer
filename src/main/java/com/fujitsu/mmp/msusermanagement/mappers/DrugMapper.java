package com.fujitsu.mmp.msusermanagement.mappers;

import com.fujitsu.mmp.msusermanagement.dto.DrugDTO;
import com.fujitsu.mmp.msusermanagement.entities.Drug;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DrugMapper {

    private final ModelMapper modelMapper;

    public DrugMapper() {
        this.modelMapper = new ModelMapper();
    }

    public Drug dtoToEntity(DrugDTO dto) {
        return modelMapper.map(dto, Drug.class);
    }

    public DrugDTO entityToDto(Drug entity) {
        return modelMapper.map(entity, DrugDTO.class);
    }

    public List<Drug> listDtoToListEntity(List<DrugDTO> listDto) {
        return listDto.stream().map(this::dtoToEntity).collect(Collectors.toList());
    }

    public List<DrugDTO> listEntityToListDto(List<Drug> listEntity) {
        return listEntity.stream().map(this::entityToDto).collect(Collectors.toList());
    }

}


