package com.fujitsu.mmp.msusermanagement.dto.individual;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fujitsu.mmp.msusermanagement.constants.ESex;
import com.fujitsu.mmp.msusermanagement.entities.HumanDisease;
import com.fujitsu.mmp.msusermanagement.entities.HumanPhenotype;
import com.fujitsu.mmp.msusermanagement.entities.LifeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndividualDTO {
    private String individualId;
    private String name;
    @JsonFormat(pattern="dd/MM/yyyy")
    private Date dateOfBirth;
    private LifeStatus lifeStatus;
    private ESex sex;
    private String comment;
    private String karyotypicSex;
    private String humanEthnicity;
    private List<HumanDisease> humanDisease;
    private List<HumanPhenotype> humanPhenotype;
    private Boolean isHuman;
}
