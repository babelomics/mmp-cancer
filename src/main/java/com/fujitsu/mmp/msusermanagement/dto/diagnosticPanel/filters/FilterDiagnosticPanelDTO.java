package com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.filters;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterDiagnosticPanelDTO {
    private String diagnosticPanelIdentifier;
    private String name;
    private String assembly;
    private String ensembleRelease;
    private String author;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date creationDateStart;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date creationDateEnd;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date deletionDateStart;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date deletionDateEnd;
    private Boolean isDeleted;
    private String search;
    private Boolean ascendingPanels;
    private Boolean descendingPanels;
}
