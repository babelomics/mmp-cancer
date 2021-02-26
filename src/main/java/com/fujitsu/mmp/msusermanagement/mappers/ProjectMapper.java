package com.fujitsu.mmp.msusermanagement.mappers;

import com.fujitsu.mmp.msusermanagement.dto.project.ProjectDTO;
import com.fujitsu.mmp.msusermanagement.entities.Project;
import com.fujitsu.mmp.msusermanagement.services.GenomicDictionaryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectMapper {

    @Autowired
    GenomicDictionaryService genomicDictionaryService;

    private final ModelMapper modelMapper;

    public ProjectMapper() {
        modelMapper = new ModelMapper();
    }

    public Project dtoToEntity(ProjectDTO dto){
        return modelMapper.map(dto, Project.class);
    }

    public ProjectDTO entityToDto(Project entity) {
        modelMapper.typeMap(Project.class, ProjectDTO.class).addMappings(mapper -> {
            mapper.map(src -> src.getDiagnosticPanels().size(),
                    ProjectDTO::setDiagnosticPanelsNumber);
            mapper.map(src -> src.getAnalyses().size(),
                    ProjectDTO::setAnalysesNumber);
            mapper.map(src -> src.getFiles().size(),
                    ProjectDTO::setFilesNumber);
            mapper.map(src -> src.getIndividuals().size(),
                    ProjectDTO::setIndividualsNumber);
            mapper.map(src -> src.getSamples().size(),
                    ProjectDTO::setSamplesNumber);
            mapper.map(src -> src.getDrugs().size(),
                    ProjectDTO::setDrugsNumber);
        });
        return modelMapper.map(entity, ProjectDTO.class);
    }

    public List<Project> listDtoToListEntity(List<ProjectDTO> listDto) {
        return listDto.stream().map(this::dtoToEntity).collect(Collectors.toList());
    }

    public List<ProjectDTO> listEntityToListDto(List<Project> listEntity) {
        return listEntity.stream().map(this::entityToDto).collect(Collectors.toList());
    }
}
