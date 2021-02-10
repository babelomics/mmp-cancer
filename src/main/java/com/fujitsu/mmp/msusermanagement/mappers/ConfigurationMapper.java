package com.fujitsu.mmp.msusermanagement.mappers;

import com.fujitsu.mmp.msusermanagement.dto.configuration.ConfigurationDTO;
import com.fujitsu.mmp.msusermanagement.entities.Configuration;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ConfigurationMapper {
    private final ModelMapper modelMapper;

    public ConfigurationMapper() {
        this.modelMapper = new ModelMapper();
    }

    public Configuration dtoToEntity(ConfigurationDTO dto) {
        return modelMapper.map(dto, Configuration.class);
    }

    public ConfigurationDTO entityToDto(Configuration entity) {
        return modelMapper.map(entity, ConfigurationDTO.class);
    }

    public List<Configuration> listDtoToListEntity(List<ConfigurationDTO> listDto) {
        return listDto.stream().map(this::dtoToEntity).collect(Collectors.toList());
    }

    public List<ConfigurationDTO> listEntityToListDto(List<Configuration> listEntity) {
        return listEntity.stream().map(this::entityToDto).collect(Collectors.toList());
    }
}
