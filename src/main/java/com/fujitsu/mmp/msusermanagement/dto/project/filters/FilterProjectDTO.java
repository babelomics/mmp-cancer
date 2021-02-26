package com.fujitsu.mmp.msusermanagement.dto.project.filters;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterProjectDTO {
    private String projectId;
    private String name;
    private String organism;
    private String description;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date creationDateStart;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date creationDateEnd;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date modificationDateStart;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date modificationDateEnd;
    private String assembly;
    private String ensemblRelease;
    private String search;

}
