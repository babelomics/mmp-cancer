package com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.exportimport;

import com.fujitsu.mmp.msusermanagement.entities.DiagnosticPanelAssociation;
import com.fujitsu.mmp.msusermanagement.entities.DiagnosticPanelFeature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiagnosticPanelExportImportDTO {
    private String id;
    private String name;
    private String description;
    private String author;
    private Set<DiagnosticPanelFeature> features;
    private Set<DiagnosticPanelAssociation> associations;
    private List<String> parentIds;
    private String creationDate;
    private String deletionDate;
    private Integer startVersion;
    private Integer endVersion;
    private String guid;
}
