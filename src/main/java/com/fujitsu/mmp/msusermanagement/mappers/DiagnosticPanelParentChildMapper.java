package com.fujitsu.mmp.msusermanagement.mappers;

import com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.DiagnosticPanelParentChildDTO;
import com.fujitsu.mmp.msusermanagement.entities.DiagnosticPanel;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DiagnosticPanelParentChildMapper {
    private final ModelMapper modelMapper;

    public DiagnosticPanelParentChildMapper() {
        this.modelMapper = new ModelMapper();
    }

    public DiagnosticPanel dtoToEntity(DiagnosticPanelParentChildDTO dto) {
        return modelMapper.map(dto, DiagnosticPanel.class);
    }

    public DiagnosticPanelParentChildDTO entityToDto(DiagnosticPanel entity) {
        return modelMapper.map(entity, DiagnosticPanelParentChildDTO.class);
    }

    public List<DiagnosticPanel> listDtoToListEntity(List<DiagnosticPanelParentChildDTO> listDto) {
        return listDto.stream().map(this::dtoToEntity).collect(Collectors.toList());
    }

    public List<DiagnosticPanelParentChildDTO> listEntityToListDto(List<DiagnosticPanel> listEntity) {
        return listEntity.stream().map(this::entityToDto).collect(Collectors.toList());
    }
}
