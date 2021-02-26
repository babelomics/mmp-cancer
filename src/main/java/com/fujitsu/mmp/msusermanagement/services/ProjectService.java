package com.fujitsu.mmp.msusermanagement.services;

import com.fujitsu.mmp.msusermanagement.constants.EAccessType;
import com.fujitsu.mmp.msusermanagement.dto.project.ProjectDTO;
import com.fujitsu.mmp.msusermanagement.dto.project.filters.FilterProjectDTO;
import com.fujitsu.mmp.msusermanagement.entities.Project;
import com.fujitsu.mmp.msusermanagement.entities.User;
import com.fujitsu.mmp.msusermanagement.entities.UserHistory;
import com.fujitsu.mmp.msusermanagement.mappers.ProjectMapper;
import com.fujitsu.mmp.msusermanagement.repositories.PermissionRepository;
import com.fujitsu.mmp.msusermanagement.repositories.ProjectRepository;
import com.fujitsu.mmp.msusermanagement.repositories.UserHistoryRepository;
import com.fujitsu.mmp.msusermanagement.repositories.UserRepository;
import com.fujitsu.mmp.msusermanagement.utility.JWTUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ProjectService {
    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectMapper projectMapper;

    @Autowired
    JWTUtility jwtUtility;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserHistoryRepository userHistoryRepository;

    @Autowired
    GenomicDictionaryService genomicDictionaryService;

    @Autowired
    PermissionRepository permissionRepository;

    public ResponseEntity<ProjectDTO> createProject(ProjectDTO projectDTO, HttpServletRequest httpServletRequest) {

        HttpStatus responseStatus = HttpStatus.CREATED;
        ProjectDTO responseBody = null;

        String token = httpServletRequest.getHeader("Authorization");
        String username = "";

        if (token != null) {
            username = jwtUtility.getUsernameFromToken(token.substring(6));
        }

        if (projectDTO.getProjectId() == null || projectDTO.getName() == null || projectDTO.getAssembly() == null){
            responseStatus= HttpStatus.UNPROCESSABLE_ENTITY;
        } else if (projectRepository.existsByProjectId(projectDTO.getProjectId())) {
            responseStatus= HttpStatus.CONFLICT;
            //TODO nombre y organismo único y que no estén vacíos
        }else {

            Project project = projectMapper.dtoToEntity(projectDTO);
            project.setCreationDate(new Date());
            project.setAuthor(username);
            project.setAnalyses(new ArrayList<>());
            project.setSamples(new ArrayList<>());
            project.setFiles(new ArrayList<>());
            project.setIndividuals(new ArrayList<>());
            project.setDrugs(new ArrayList<>());
            project.setDiagnosticPanels(new ArrayList<>());

            Project createdProject = projectRepository.save(project);
            responseBody = projectMapper.entityToDto(createdProject);
        }

        return new ResponseEntity<>(responseBody,responseStatus);
    }

    public ResponseEntity<Page<ProjectDTO>> listProject(Pageable pageable, FilterProjectDTO filterProjectDTO, HttpServletRequest httpServletRequest) {
        HttpStatus responseStatus = HttpStatus.OK;
        Page<ProjectDTO> responseBody;

        String token = httpServletRequest.getHeader("Authorization");
        String username = "";

        if (token != null) {
            username = jwtUtility.getUsernameFromToken(token.substring(6));
        }

        Page<Project> pageEntity = projectRepository.findProjectByFilters
                (filterProjectDTO, pageable);

        List<ProjectDTO> projectDTOList = projectMapper.listEntityToListDto(pageEntity.getContent());

        for (ProjectDTO project : projectDTOList) {
            project.setOrganism(genomicDictionaryService.getMetaAssembly(project.getAssembly()).getAssembly().getSpecies().getTaxonomyId().toString());
            if(userRepository.findByIdentifier(username).getUserType().equals("Admin")){
                project.setAccessType(String.valueOf(EAccessType.DIRECT));
            }else {
                project.setAccessType(String.valueOf(permissionRepository.findByUserId(username).stream().anyMatch(temp -> temp.getEntityId().equals(filterProjectDTO.getProjectId())) ? EAccessType.DIRECT : EAccessType.INDIRECT));
            }
        }

        responseBody = new PageImpl<>(projectDTOList, pageable, pageEntity.getTotalElements());

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<ProjectDTO> getProject(String projectId) {
        HttpStatus responseStatus = HttpStatus.OK;
        ProjectDTO responseBody = null;

        Project entity = projectRepository.findByProjectId(projectId);

        if (entity == null) {
            responseStatus = HttpStatus.NOT_FOUND;
        }else{
            responseBody = projectMapper.entityToDto(entity);
            responseBody.setAuthor(getFormattedAuthor(entity.getAuthor()));
            responseBody.setOrganism(genomicDictionaryService.getMetaAssembly(entity.getAssembly()).getAssembly().getSpecies().getTaxonomyId().toString());
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<ProjectDTO> updateProject(String projectId, ProjectDTO projectDTO) {
        HttpStatus responseStatus = HttpStatus.OK;
        ProjectDTO responseBody = null;

        if(projectId == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else if (!projectId.equals(projectDTO.getProjectId())) {
            responseStatus = HttpStatus.BAD_REQUEST;
        } else {
            Project entity = projectRepository.findByProjectId(projectId);
            if (entity == null) {
                responseStatus = HttpStatus.NOT_FOUND;
            } else if (projectDTO.getName() == null) {
                responseStatus = HttpStatus.BAD_REQUEST;
            }else if (entity.getDeletionDate() != null){
                responseStatus = HttpStatus.FORBIDDEN;
            } else {
                Project entityToSave = projectMapper.dtoToEntity(projectDTO);
                entityToSave.setGuid(entity.getGuid());
                if(projectDTO.getSamplesNumber() == 0 && projectDTO.getIndividualsNumber() == 0 && projectDTO.getFilesNumber() == 0 ){
                    if (entity.getSamples().size() == 0 && entity.getIndividuals().size() == 0 && entity.getFiles().size() == 0) {
                        entityToSave.setModificationDate(new Date());
                        entityToSave.setName(projectDTO.getName());
                        entityToSave.setDescription(projectDTO.getDescription() != null ? projectDTO.getDescription() : "");
                        entityToSave.setEnsemblRelease(projectDTO.getEnsemblRelease());
                        entityToSave.setAssembly(projectDTO.getAssembly());
                    }else{
                        responseStatus= HttpStatus.CONFLICT;
                    }
                }else{
                    entityToSave.setModificationDate(new Date());
                    entityToSave.setName(projectDTO.getName());
                    entityToSave.setDescription(projectDTO.getDescription() != null ? projectDTO.getDescription() : "");
                }

                entityToSave.setAuthor(entity.getAuthor());
                entityToSave.setSamples(entity.getSamples());
                entityToSave.setFiles(entity.getFiles());
                entityToSave.setIndividuals(entity.getIndividuals());
                entityToSave.setDrugs(entity.getDrugs());
                entityToSave.setDiagnosticPanels(entity.getDiagnosticPanels());
                entityToSave.setAnalyses(entity.getAnalyses());

                entityToSave = projectRepository.save(entityToSave);
                responseBody = projectMapper.entityToDto(entityToSave);
            }
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<ProjectDTO> deleteProject(String projectId) {
        HttpStatus responseStatus = HttpStatus.OK;
        ProjectDTO responseBody = null;

        Project elementToDelete = projectRepository.findByProjectId(projectId);

        if (projectId == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else if (elementToDelete == null) {
            responseStatus = HttpStatus.NOT_FOUND;
        } else {
            //TODO copia para borrado físico
            elementToDelete.setDeletionDate(new Date());

            elementToDelete = projectRepository.save(elementToDelete);
            responseBody = projectMapper.entityToDto(elementToDelete);

        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    //TODO: Create a utility class with this method below
    private String getFormattedAuthor (String username){
        String formattedAuthor = "";
        if(username != null){
            User user = userRepository.findByIdentifier(username);

            if(user == null) {
                UserHistory userHistory = userHistoryRepository.findByIdentifier(username);
                formattedAuthor = userHistory != null ? userHistory.getIdentifier() + " - " + userHistory.getFirstName() + " " + userHistory.getLastName() : "Undefined";
            }else{
                formattedAuthor =  user.getIdentifier() + " - " + user.getFirstName() + " " + user.getLastName();
            }
        }
        return formattedAuthor;
    }
}
