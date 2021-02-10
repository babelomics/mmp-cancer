package com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fujitsu.mmp.msusermanagement.entities.DiagnosticPanelReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticPanelSetDTO {
    private String diagnosticPanelSetIdentifier;
    private String name;
    private String description;
    private DiagnosticPanelReference reference;
    private String author;
    @JsonFormat(pattern="dd/MM/yyyy")
    private Date creationDate;
    @JsonFormat(pattern="dd/MM/yyyy")
    private Date deletionDate;
    private Integer panelsNumber;
    private Boolean isHuman;
}
