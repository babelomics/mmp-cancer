package com.fujitsu.mmp.msusermanagement.mappers;

import com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.DiagnosticPanelDTO;
import com.fujitsu.mmp.msusermanagement.entities.DiagnosticPanel;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DiagnosticPanelMapper {
    private final ModelMapper modelMapper;

    public DiagnosticPanelMapper() {
        this.modelMapper = new ModelMapper();
    }

    public DiagnosticPanel dtoToEntity(DiagnosticPanelDTO dto) {
        return modelMapper.map(dto, DiagnosticPanel.class);
    }

    public DiagnosticPanelDTO entityToDto(DiagnosticPanel entity) {
        return modelMapper.map(entity, DiagnosticPanelDTO.class);
    }

    public List<DiagnosticPanel> listDtoToListEntity(List<DiagnosticPanelDTO> listDto) {
        return listDto.stream().map(this::dtoToEntity).collect(Collectors.toList());
    }

    public List<DiagnosticPanelDTO> listEntityToListDto(List<DiagnosticPanel> listEntity) {
        return listEntity.stream().map(this::entityToDto).collect(Collectors.toList());
    }
}
