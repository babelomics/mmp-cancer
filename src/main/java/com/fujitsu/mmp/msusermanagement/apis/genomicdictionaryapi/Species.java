package com.fujitsu.mmp.msusermanagement.apis.genomicdictionaryapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Species {
    private Integer taxonomyId;
    private String scientificName;
    private String commonName;
}
