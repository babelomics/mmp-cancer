package com.fujitsu.mmp.msusermanagement.apis.genomicdictionaryapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegionDTO {
    private String regionIdentifier;
    private String chromosome;
    private String initPosition;
    private String endPosition;
}
