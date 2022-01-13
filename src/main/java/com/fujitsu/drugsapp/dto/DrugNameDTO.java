package com.fujitsu.drugsapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DrugNameDTO {

    private UUID id;

    private String name;

    private DrugSourceDTO drugSource;
}
