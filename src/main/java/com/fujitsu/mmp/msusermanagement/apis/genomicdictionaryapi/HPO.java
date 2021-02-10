package com.fujitsu.mmp.msusermanagement.apis.genomicdictionaryapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HPO {
    private String hpoId;
    private String name;
    private String def;
    private List<String> altID;
    private List<String> parents;
    private List<String> children;
}
