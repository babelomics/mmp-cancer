package com.fujitsu.mmp.msusermanagement.entities;

import com.fujitsu.mmp.msusermanagement.constants.EOntology;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HumanDisease {
    private String diseaseId;
    private Date dateOfDiagnosis;
    private EOntology ontology;
    private String comment;
    private String description;
}
