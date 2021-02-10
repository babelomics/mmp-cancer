package com.fujitsu.mmp.msusermanagement.apis.genomicdictionaryapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VariantDTO {
    private String variantIdentifier;
    private String chromosomeSequence;
    private String initPosition;
    private String reference;
    private String alternative;
    private Boolean isChildren;
}
