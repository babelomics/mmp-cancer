package com.fujitsu.mmp.msusermanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fujitsu.mmp.msusermanagement.entities.AlternativeNames;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrugDTO {
    private String id;
    private Long version;
    private String standardName;
    private String commonName;
    private List<AlternativeNames> alternativeNames;
    private Boolean available;
    private Double cost;
    @JsonFormat(pattern="dd/MM/yyyy")
    private Date creationDate;
    @JsonFormat(pattern="dd/MM/yyyy")
    private Date deletionDate;
    private String userId;
    private Long previousVersion;
}
