package com.fujitsu.mmp.msusermanagement.mappers;

import com.fujitsu.mmp.msusermanagement.apis.genomicdictionaryapi.VariantDTO;
import com.fujitsu.mmp.msusermanagement.entities.Variant;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VariantMapper {
    private final ModelMapper modelMapper;

    public VariantMapper() {
        this.modelMapper = new ModelMapper();
    }

    public Variant dtoToEntity(VariantDTO dto) {
        return modelMapper.map(dto, Variant.class);
    }

    public VariantDTO entityToDTO(Variant entity) {return modelMapper.map(entity, VariantDTO.class);}

    public List<Variant> listDTOToListEntity(List<VariantDTO> variantDto){
        return variantDto.stream().map(this::dtoToEntity).collect(Collectors.toList());
    }

    public List<VariantDTO> listEntityToListDto(List<Variant> listEntity) {
        return listEntity.stream().map(this::entityToDTO).collect(Collectors.toList());
    }
}
