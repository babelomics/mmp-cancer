package com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fujitsu.mmp.msusermanagement.entities.DiagnosticPanelAssociation;
import com.fujitsu.mmp.msusermanagement.entities.DiagnosticPanelFeature;
import com.fujitsu.mmp.msusermanagement.constants.EStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiagnosticPanelDTO {
    private String diagnosticPanelIdentifier;
    private String name;
    private String description;
    private String author;
    private Set<DiagnosticPanelFeature> features;
    private Set<DiagnosticPanelAssociation> associations;
    @JsonFormat(pattern="dd/MM/yyyy")
    private Date creationDate;
    @JsonFormat(pattern="dd/MM/yyyy")
    private Date deletionDate;
    private String diagnosticPanelSetIdentifier;
    private String previousVersion;
    private EStatus status;
    private Integer genessNumber;
    private Integer transcNumber;
    private Integer regionsNumber;
    private Integer variantsNumber;
    private List<String> parentIds;
    private Boolean ascendingPanels;
    private Boolean descendingPanels;
    private String guid;
}
