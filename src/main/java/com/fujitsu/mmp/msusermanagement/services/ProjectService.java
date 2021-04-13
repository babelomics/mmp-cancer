package com.fujitsu.mmp.msusermanagement.services;

import com.fujitsu.mmp.msusermanagement.constants.EAccessType;
import com.fujitsu.mmp.msusermanagement.constants.EPermissionAction;
import com.fujitsu.mmp.msusermanagement.constants.EPermissionEntityType;
import com.fujitsu.mmp.msusermanagement.constants.UserConstants;
import com.fujitsu.mmp.msusermanagement.dto.permission.PermissionDTO;
import com.fujitsu.mmp.msusermanagement.dto.project.ProjectDTO;
import com.fujitsu.mmp.msusermanagement.dto.project.filters.FilterProjectDTO;
import com.fujitsu.mmp.msusermanagement.entities.Permission;
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
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

    @Autowired
    UserService userService;

    @Autowired
    PermissionService permissionService;

    public ResponseEntity<ProjectDTO> createProject(ProjectDTO projectDTO, HttpServletRequest httpServletRequest) {

        HttpStatus responseStatus = HttpStatus.CREATED;
        ProjectDTO responseBody = null;

        String token = httpServletRequest.getHeader("Authorization");
        String username = "";

        if (token != null) {
            username = jwtUtility.getUsernameFromToken(token.substring(6));
        }

        PermissionDTO createProjectUndefined = new PermissionDTO(EPermissionAction.CREATE.getValue(), EPermissionEntityType.PROJECTS.getValue(), "undefined");

        if (projectDTO.getProjectId().isBlank() || projectDTO.getName().isBlank() || projectDTO.getAssembly().isBlank()){
            responseStatus= HttpStatus.UNPROCESSABLE_ENTITY;
        } else if (!permissionService.hasAccess(httpServletRequest, createProjectUndefined)) {
            responseStatus = HttpStatus.FORBIDDEN;
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
            project.setOrganism(genomicDictionaryService.getMetaAssembly(project.getAssembly()).getAssembly().getSpecies().getTaxonomyId().toString());

            createProjectPermissions(username, projectDTO.getProjectId());

            Project createdProject = projectRepository.save(project);
            responseBody = projectMapper.entityToDto(createdProject);
        }

        return new ResponseEntity<>(responseBody,responseStatus);
    }

    public ResponseEntity<Page<ProjectDTO>> listProject(Pageable pageable, FilterProjectDTO filterProjectDTO, HttpServletRequest httpServletRequest) {
        HttpStatus responseStatus = HttpStatus.OK;
        Page<ProjectDTO> responseBody;

        Page<Project> pageEntity = projectRepository.findProjectByFilters
                (filterProjectDTO, pageable, userService.getPermissions(httpServletRequest).getBody());

        List<ProjectDTO> projectDTOList = projectMapper.listEntityToListDto(pageEntity.getContent());

        for (ProjectDTO project : projectDTOList) {
            project.setOrganism(genomicDictionaryService.getMetaAssembly(project.getAssembly()).getAssembly().getSpecies().getTaxonomyId().toString());

            EAccessType accessType = getUserAccessType(httpServletRequest, project.getProjectId());

            if(EAccessType.DIRECT.equals(accessType)){
                project.setAccessType(String.valueOf(EAccessType.DIRECT));
            } else {
                project.setAccessType(String.valueOf(EAccessType.INDIRECT));
            }
        }

        responseBody = new PageImpl<>(projectDTOList, pageable, pageEntity.getTotalElements());

        return new ResponseEntity<>(responseBody, responseStatus);
    }

    private EAccessType getUserAccessType(HttpServletRequest httpServletRequest, String projectId) {
        EAccessType accessType = null;

        List<PermissionDTO> permissionList = new ArrayList<>();
        PermissionDTO readPermission = new PermissionDTO(EPermissionAction.READ.getValue(), EPermissionEntityType.PROJECT.getValue(), projectId);
        PermissionDTO readProjectUndefined = new PermissionDTO(EPermissionAction.READ.getValue(), EPermissionEntityType.PROJECTS.getValue(), "undefined");

        permissionList.add(readPermission);
        permissionList.add(readProjectUndefined);

        if (permissionList.stream().noneMatch(permission -> permissionService.hasAccess(httpServletRequest, permission))) {

            permissionList.clear();

            PermissionDTO individualPermission = new PermissionDTO(EPermissionAction.READ.getValue(), EPermissionEntityType.INDIVIDUALS.getValue(), projectId);
            PermissionDTO samplePermission = new PermissionDTO(EPermissionAction.READ.getValue(), EPermissionEntityType.SAMPLES.getValue(), projectId);
            PermissionDTO drugPermission = new PermissionDTO(EPermissionAction.READ.getValue(), EPermissionEntityType.DRUGS.getValue(), projectId);
            PermissionDTO diagnosticPanelsPermission = new PermissionDTO(EPermissionAction.READ.getValue(), EPermissionEntityType.DIAGNOSTIC_PANELS.getValue(), projectId);
            PermissionDTO analysisPermission = new PermissionDTO(EPermissionAction.READ.getValue(), EPermissionEntityType.ANALYSES.getValue(), projectId);

            permissionList.add(individualPermission);
            permissionList.add(samplePermission);
            permissionList.add(drugPermission);
            permissionList.add(diagnosticPanelsPermission);
            permissionList.add(analysisPermission);

            if (permissionList.stream().anyMatch(permission -> permissionService.hasAccess(httpServletRequest, permission))) {
                accessType = EAccessType.INDIRECT;
            }
        } else {
            accessType = EAccessType.DIRECT;
        }
        return accessType;
    }

    public ResponseEntity<ProjectDTO> getProject(String projectId, HttpServletRequest httpServletRequest) {
        HttpStatus responseStatus = HttpStatus.OK;
        ProjectDTO responseBody = null;

        if(projectId == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else {
            Project entity = projectRepository.findByProjectId(projectId);

            if(entity == null){
                responseStatus = HttpStatus.NOT_FOUND;
            } else {

                ProjectDTO project = projectMapper.entityToDto(entity);

                List<PermissionDTO> permissionList = new ArrayList<>();
                PermissionDTO readPermission = new PermissionDTO(EPermissionAction.READ.getValue(), EPermissionEntityType.PROJECT.getValue(), projectId);
                PermissionDTO readProjectUndefined = new PermissionDTO(EPermissionAction.READ.getValue(), EPermissionEntityType.PROJECTS.getValue(), "undefined");

                permissionList.add(readPermission);
                permissionList.add(readProjectUndefined);

                if(permissionList.stream().noneMatch(permission -> permissionService.hasAccess(httpServletRequest, permission))){

                    boolean hasIndirectAccess = false;

                    responseBody = new ProjectDTO();

                    PermissionDTO individualPermission = new PermissionDTO(EPermissionAction.READ.getValue(), EPermissionEntityType.INDIVIDUALS.getValue(), projectId);
                    PermissionDTO samplePermission = new PermissionDTO(EPermissionAction.READ.getValue(),EPermissionEntityType.SAMPLES.getValue(), projectId);
                    PermissionDTO drugPermission = new PermissionDTO(EPermissionAction.READ.getValue(),EPermissionEntityType.DRUGS.getValue(),projectId);
                    PermissionDTO diagnosticPanelsPermission = new PermissionDTO(EPermissionAction.READ.getValue(),EPermissionEntityType.DIAGNOSTIC_PANELS.getValue(),projectId);
                    PermissionDTO analysisPermission = new PermissionDTO(EPermissionAction.READ.getValue(),EPermissionEntityType.ANALYSES.getValue(),projectId);

                    if(permissionService.hasAccess(httpServletRequest, individualPermission)){
                        responseBody.setIndividualsNumber(project.getIndividualsNumber());
                        hasIndirectAccess = true;
                    }

                    if(permissionService.hasAccess(httpServletRequest, samplePermission)){
                        responseBody.setSamplesNumber(project.getSamplesNumber());
                        hasIndirectAccess = true;
                    }

                    if(permissionService.hasAccess(httpServletRequest, drugPermission)){
                        responseBody.setDrugsNumber(project.getDrugsNumber());
                        hasIndirectAccess = true;
                    }

                    if(permissionService.hasAccess(httpServletRequest, diagnosticPanelsPermission)){
                        responseBody.setDiagnosticPanelsNumber(project.getDiagnosticPanelsNumber());
                        hasIndirectAccess = true;
                    }

                    if(permissionService.hasAccess(httpServletRequest, analysisPermission)){
                        responseBody.setAnalysesNumber(project.getAnalysesNumber());
                        hasIndirectAccess = true;
                    }

                    if(!hasIndirectAccess){
                        responseStatus = HttpStatus.FORBIDDEN;
                    }else{
                        responseBody.setProjectId(project.getProjectId());
                        responseBody.setName(project.getName());
                        responseBody.setDescription(project.getDescription());
                        responseBody.setOrganism(genomicDictionaryService.getMetaAssembly(entity.getAssembly()).getAssembly().getSpecies().getTaxonomyId().toString());
                        responseBody.setEnsemblRelease(project.getEnsemblRelease());
                        responseBody.setAssembly(project.getAssembly());
                    }
                } else {
                    responseBody = project;
                    responseBody.setAuthor(getFormattedAuthor(entity.getAuthor()));
                    responseBody.setOrganism(genomicDictionaryService.getMetaAssembly(entity.getAssembly()).getAssembly().getSpecies().getTaxonomyId().toString());
                }
            }
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }

    public ResponseEntity<ProjectDTO> updateProject(String projectId, ProjectDTO projectDTO, HttpServletRequest httpServletRequest) {
        HttpStatus responseStatus = HttpStatus.OK;
        ProjectDTO responseBody = null;

        if(projectId == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else if (!projectId.equals(projectDTO.getProjectId())) {
            responseStatus = HttpStatus.BAD_REQUEST;
        } else {
            Project entity = projectRepository.findByProjectId(projectId);

            List<PermissionDTO> permissionList = new ArrayList<>();
            PermissionDTO updateProject = new PermissionDTO(EPermissionAction.UPDATE.getValue(), EPermissionEntityType.PROJECT.getValue(), projectId);
            PermissionDTO updateProjectUndefined = new PermissionDTO(EPermissionAction.UPDATE.getValue(), EPermissionEntityType.PROJECTS.getValue(), "undefined");

            permissionList.add(updateProject);
            permissionList.add(updateProjectUndefined);

            if (permissionList.stream().noneMatch(permission -> permissionService.hasAccess(httpServletRequest, permission))) {
                responseStatus = HttpStatus.FORBIDDEN;
            } else if (entity == null) {
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

    public ResponseEntity<Void> deleteProject(String projectId, HttpServletRequest httpServletRequest) {
        HttpStatus responseStatus = HttpStatus.NO_CONTENT;

        if(projectId == null) {
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } else {
            Project elementToDelete = projectRepository.findByProjectId(projectId);

            List<PermissionDTO> permissionList = new ArrayList<>();
            PermissionDTO deleteProject = new PermissionDTO(EPermissionAction.DELETE.getValue(), EPermissionEntityType.PROJECT.getValue(), projectId);
            PermissionDTO deleteProjectUndefined = new PermissionDTO(EPermissionAction.DELETE.getValue(), EPermissionEntityType.PROJECTS.getValue(), "undefined");

            permissionList.add(deleteProject);
            permissionList.add(deleteProjectUndefined);

            if (permissionList.stream().noneMatch(permission -> permissionService.hasAccess(httpServletRequest, permission))) {
                responseStatus = HttpStatus.FORBIDDEN;
            } else if (elementToDelete == null) {
                responseStatus = HttpStatus.NOT_FOUND;
            } else {
                //TODO copia para borrado físico
                elementToDelete.setDeletionDate(new Date());

                projectRepository.save(elementToDelete);
            }
        }
        return new ResponseEntity<>(responseStatus);
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

    private void createProjectPermissions(String username, String projectId) {
        List<Permission> permissionList = new ArrayList<>();

        Permission updatePermissions = new Permission("update","permissions",projectId, username);
        Permission deleteProject = new Permission("delete","project",projectId, username);
        Permission updateProject = new Permission("update","project",projectId, username);
        Permission readProject = new Permission("read","project",projectId, username);
        Permission deleteAnalyses = new Permission("delete","analyses",projectId, username);
        Permission updateAnalyses = new Permission("update","analyses",projectId, username);
        Permission createAnalyses = new Permission("create","analyses",projectId, username);
        Permission deleteSamples = new Permission("delete","samples",projectId, username);
        Permission updateSamples = new Permission("update","samples",projectId, username);
        Permission createSamples = new Permission("create","samples",projectId, username);
        Permission readSamples = new Permission("read","samples",projectId, username);
        Permission deleteIndividuals = new Permission("delete","individuals",projectId, username);
        Permission updateIndividuals = new Permission("update","individuals",projectId, username);
        Permission createIndividuals = new Permission("create","individuals",projectId, username);
        Permission readIndividuals = new Permission("read","individuals",projectId, username);
        Permission deleteDrugs = new Permission("delete","drugs",projectId, username);
        Permission updateDrugs = new Permission("update","drugs",projectId, username);
        Permission createDrugs = new Permission("create","drugs",projectId, username);
        Permission readDrugs = new Permission("read","drugs",projectId, username);
        Permission deletePanels = new Permission("delete","panels",projectId, username);
        Permission updatePanes = new Permission("update","panels",projectId, username);
        Permission createPanels = new Permission("create","panels",projectId, username);
        Permission readPanels = new Permission("read","panels",projectId, username);

        permissionList.add(updatePermissions);
        permissionList.add(deleteProject);
        permissionList.add(updateProject);
        permissionList.add(readProject);
        permissionList.add(deleteAnalyses);
        permissionList.add(updateAnalyses);
        permissionList.add(createAnalyses);
        permissionList.add(deleteSamples);
        permissionList.add(updateSamples);
        permissionList.add(createSamples);
        permissionList.add(readSamples);
        permissionList.add(deleteIndividuals);
        permissionList.add(updateIndividuals);
        permissionList.add(createIndividuals);
        permissionList.add(readIndividuals);
        permissionList.add(deleteDrugs);
        permissionList.add(updateDrugs);
        permissionList.add(createDrugs);
        permissionList.add(readDrugs);
        permissionList.add(deletePanels);
        permissionList.add(updatePanes);
        permissionList.add(createPanels);
        permissionList.add(readPanels);

        permissionRepository.saveAll(permissionList);
    }
}
