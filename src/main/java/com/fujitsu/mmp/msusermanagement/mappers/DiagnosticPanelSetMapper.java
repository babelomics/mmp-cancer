package com.fujitsu.mmp.msusermanagement.mappers;

import com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.DiagnosticPanelSetDTO;
import com.fujitsu.mmp.msusermanagement.entities.DiagnosticPanelSet;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DiagnosticPanelSetMapper {
    private final ModelMapper modelMapper;

    public DiagnosticPanelSetMapper() {
        this.modelMapper = new ModelMapper();
    }

    public DiagnosticPanelSet dtoToEntity(DiagnosticPanelSetDTO dto) {
        return modelMapper.map(dto, DiagnosticPanelSet.class);
    }

    public DiagnosticPanelSetDTO entityToDto(DiagnosticPanelSet entity) {
        return modelMapper.map(entity, DiagnosticPanelSetDTO.class);
    }

    public List<DiagnosticPanelSet> listDtoToListEntity(List<DiagnosticPanelSetDTO> listDto) {
        return listDto.stream().map(this::dtoToEntity).collect(Collectors.toList());
    }

    public List<DiagnosticPanelSetDTO> listEntityToListDto(List<DiagnosticPanelSet> listEntity) {
        return listEntity.stream().map(this::entityToDto).collect(Collectors.toList());
    }
}
