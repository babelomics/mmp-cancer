package com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class DiagnosticPanelParentChildDTO {
    private String guid;
    private String diagnosticPanelIdentifier;
    private String name;
    private String description;
    private List<String> parentIds;
    private Boolean toDelete;
}
