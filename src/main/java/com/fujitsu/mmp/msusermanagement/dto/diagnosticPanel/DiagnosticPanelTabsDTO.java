package com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel;

import com.fujitsu.mmp.msusermanagement.apis.genomicdictionaryapi.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticPanelTabsDTO {
    private String diagnosticPanelIdentifier;
    private String name;
    private String description;
    private String author;
    private Date creationDate;
    private Date deletionDate;
    private String diagnosticPanelSetIdentifier;
    private String assembly;
    private String ensmblRelease;
    private Boolean isHuman;
    private String guid;
    private String nextVersion;
    private String previousVersion;
    private List<ICD10> icd10List;
    private List<HPO> hpoList;
    private List<Gene> geneList;
    private List<Transcript> transcriptList;
    private List<RegionDTO> regionList;
    private List<VariantDTO> variantList;
    private List<DiagnosticPanelParentChildDTO> ascendants;
    private List <DiagnosticPanelParentChildDTO> descendants;
    private Boolean isNewVersion;
    private Integer startVersion;
    private Integer endVersion;
}
