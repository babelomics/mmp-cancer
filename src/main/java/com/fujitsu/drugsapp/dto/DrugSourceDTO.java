package com.fujitsu.drugsapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.net.URL;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DrugSourceDTO {

    private UUID id;

    private String shortName;

    private URL url;

}
