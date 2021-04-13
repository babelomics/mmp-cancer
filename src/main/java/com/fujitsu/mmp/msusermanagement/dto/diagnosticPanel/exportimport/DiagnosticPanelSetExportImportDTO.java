package com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.exportimport;

import com.fujitsu.mmp.msusermanagement.entities.DiagnosticPanelReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiagnosticPanelSetExportImportDTO {
    private String id;
    private String name;
    private String description;
    private DiagnosticPanelReference reference;
    private String author;
    private String creationDate;
    private String deletionDate;
    private String exportDate;
    private Integer currentVersion;
    private List<DiagnosticPanelExportImportDTO> panels;
}
