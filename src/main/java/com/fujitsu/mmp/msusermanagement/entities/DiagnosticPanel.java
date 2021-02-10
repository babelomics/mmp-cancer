package com.fujitsu.mmp.msusermanagement.entities;

import com.fujitsu.mmp.msusermanagement.constants.EStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticPanel {
    @Id
    private String guid;
    private String diagnosticPanelIdentifier;
    private String name;
    private String description;
    private String author;
    private Set<DiagnosticPanelFeature> features;
    private Set<DiagnosticPanelAssociation> associations;
    private List<String> parentIds;
    private EStatus status;
    private Date creationDate;
    private Date deletionDate;
    private String diagnosticPanelSetIdentifier;
    private String previousVersion;
}
