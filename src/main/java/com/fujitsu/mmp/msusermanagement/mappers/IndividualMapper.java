package com.fujitsu.mmp.msusermanagement.mappers;

import com.fujitsu.mmp.msusermanagement.dto.individual.IndividualDTO;
import com.fujitsu.mmp.msusermanagement.entities.Individual;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class IndividualMapper {
    private final ModelMapper modelMapper;

    public IndividualMapper() {
        modelMapper = new ModelMapper();
    }

    public Individual dtoToEntity(IndividualDTO dto){
        return modelMapper.map(dto, Individual.class);
    }

    public IndividualDTO entityToDto(Individual entity) {
        return modelMapper.map(entity, IndividualDTO.class);
    }

    public List<Individual> listDtoToListEntity(List<IndividualDTO> listDto) {
        return listDto.stream().map(this::dtoToEntity).collect(Collectors.toList());
    }

    public List<IndividualDTO> listEntityToListDto(List<Individual> listEntity) {
        return listEntity.stream().map(this::entityToDto).collect(Collectors.toList());
    }
}
