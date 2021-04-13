package com.fujitsu.mmp.msusermanagement.dto.individual.filters;

import com.fujitsu.mmp.msusermanagement.constants.ESex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterIndividualDTO {
    private String hpo;
    private String icd10;
    private ESex sex;
    private String karyotypicSex;
    private String search;
    private String individualId;
    private String name;
    private String comment;
}
