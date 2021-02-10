package com.fujitsu.mmp.msusermanagement.apis.genomicdictionaryapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ICD10 {
    private String id;
    private String desc;
    private ICD10Object parent;
    private List<ICD10Object> children;
    private String type;
}
