package com.fujitsu.mmp.msusermanagement.controllers;

import com.fujitsu.mmp.msusermanagement.dto.project.ProjectDTO;
import com.fujitsu.mmp.msusermanagement.dto.project.filters.FilterProjectDTO;
import com.fujitsu.mmp.msusermanagement.services.ProjectService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;

@CrossOrigin(origins = "*", maxAge = 3600, exposedHeaders = "Authorization")
@RequestMapping("/api/projects")
@RestController
public class ProjectController {
    @Autowired
    private ProjectService projectService;

    /**
     * List all projects with pagination
     * @return list of all projects entities found
     */
    @GetMapping("/list")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "Results page you want to retrieve (0..N)", defaultValue = "0"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "Number of records per page.", defaultValue = "20"),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "Sorting criteria in the format: property(,asc|desc). " +
                            "Default sort order is ascending. " +
                            "Multiple sort criteria are supported.")
    })
    public ResponseEntity<Page<ProjectDTO>> listProject (@ApiIgnore("Ignored because swagger ui shows the wrong params, instead they are explained in the implicit params") Pageable pageable, FilterProjectDTO filterProjectDTO, HttpServletRequest httpServletRequest) {
        return projectService.listProject(pageable, filterProjectDTO, httpServletRequest);
    }

    /**
     * Create a new Project
     * @param projectDTO projectDTO to create
     * @return
     */
    @PostMapping("/project")
    public ResponseEntity<ProjectDTO> createProject(@RequestBody ProjectDTO projectDTO, HttpServletRequest httpServletRequest) {
        return projectService.createProject(projectDTO, httpServletRequest);
    }

    /**
     * Get details of projects.
     * @param projectId: identifier of the entity to retrieve.
     * @return
     */
    @GetMapping("/project/id/{projectId}")
    public ResponseEntity<ProjectDTO> getProject(@PathVariable String projectId, HttpServletRequest httpServletRequest){
        return projectService.getProject(projectId, httpServletRequest);
    }

    /**
     * Update an project entity
     * @param projectId: identifier of the entity
     * @param projectDTO entity to update
     * @return projectDTO updated
     */
    @PutMapping("/project/id/{projectId}")
    public ResponseEntity<ProjectDTO> updateProject(@PathVariable String projectId, @RequestBody ProjectDTO projectDTO, HttpServletRequest httpServletRequest) {
        return projectService.updateProject(projectId, projectDTO, httpServletRequest);
    }

    /**
     * Delete a project.
     * @param projectId
     * @return
     */
    @DeleteMapping("/id/{projectId}/project")
    public ResponseEntity<Void> deleteProject(@PathVariable String projectId, HttpServletRequest httpServletRequest){
        return projectService.deleteProject(projectId, httpServletRequest);
    }
}
