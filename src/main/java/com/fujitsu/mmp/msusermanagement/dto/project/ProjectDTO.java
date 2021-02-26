package com.fujitsu.mmp.msusermanagement.dto.project;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fujitsu.mmp.msusermanagement.entities.DiagnosticPanel;
import com.fujitsu.mmp.msusermanagement.entities.Drug;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private String projectId;
    private String name;
    private String description;
    private String author;
    @JsonFormat(pattern="dd/MM/yyyy")
    private Date creationDate;
    @JsonFormat(pattern="dd/MM/yyyy")
    private Date modificationDate;
    @JsonFormat(pattern="dd/MM/yyyy")
    private Date deletionDate;
    private String assembly;
    private String ensemblRelease;
    private Integer samplesNumber;
    private Integer individualsNumber;
    private Integer analysesNumber;
    private Integer filesNumber;
    private Integer diagnosticPanelsNumber;
    private Integer drugsNumber;
    private String organism;
    private String accessType;
}