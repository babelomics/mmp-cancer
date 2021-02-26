package com.fujitsu.mmp.msusermanagement.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "projects")
public class Project {
    @Id
    private String guid;
    private String projectId;
    private String name;
    private String description;
    private String author;
    private Date creationDate;
    private Date modificationDate;
    private Date deletionDate;
    private String assembly;
    private String ensemblRelease;
    private List<Object> samples;
    private List<Object> files;
    private List<Object> individuals;
    private List<Object> drugs;
    private List<Object> diagnosticPanels;
    private List<Object> analyses;
}
