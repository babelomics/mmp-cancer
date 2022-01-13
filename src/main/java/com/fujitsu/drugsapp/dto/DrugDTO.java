package com.fujitsu.drugsapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DrugDTO {

    private UUID id;

    private UUID startUpdate;

    private UUID endUpdate;

    private String standardName;

    private String commonName;

    private UUID previousVersion;

    private UUID nextVersion;

    private List<DrugNameDTO> drugNames;

}
