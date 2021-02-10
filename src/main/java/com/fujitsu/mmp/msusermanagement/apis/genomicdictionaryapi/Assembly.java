package com.fujitsu.mmp.msusermanagement.apis.genomicdictionaryapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Assembly {
    private String accession;
    private String accessionType;
    private String name;
    private String longName;
    private String ucscAlias;
    private Species species;
}
