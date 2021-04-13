package com.fujitsu.mmp.msusermanagement.entities;


import com.fujitsu.mmp.msusermanagement.constants.ESex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "individuals")
public class Individual {
    @Id
    private String guid;
    private String individualId;
    private String name;
    private Date dateOfBirth;
    private LifeStatus lifeStatus;
    private ESex sex;
    private String karyotypicSex;
    private String comment;
    private Date creationDate;
    private Date modificationDate;
    private Date deletionDate;
    private List<HumanPhenotype> humanPhenotype;
    private String humanEthnicity;
    private List<HumanDisease> humanDisease;
}
