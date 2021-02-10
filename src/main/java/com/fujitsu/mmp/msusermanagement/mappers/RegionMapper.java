package com.fujitsu.mmp.msusermanagement.mappers;

import com.fujitsu.mmp.msusermanagement.apis.genomicdictionaryapi.RegionDTO;
import com.fujitsu.mmp.msusermanagement.entities.Region;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RegionMapper {
    private final ModelMapper modelMapper;

    public RegionMapper() {
        this.modelMapper = new ModelMapper();
    }

    public Region dtoToEntity(RegionDTO dto) {
        return modelMapper.map(dto, Region.class);
    }

    public RegionDTO entityToDTO(Region entity) {return modelMapper.map(entity, RegionDTO.class);}

    public List<Region> listDTOToListEntity(List<RegionDTO> regionDto){
        return regionDto.stream().map(this::dtoToEntity).collect(Collectors.toList());
    }

    public List<RegionDTO> listEntityToListDto(List<Region> listEntity) {
        return listEntity.stream().map(this::entityToDTO).collect(Collectors.toList());
    }
}
