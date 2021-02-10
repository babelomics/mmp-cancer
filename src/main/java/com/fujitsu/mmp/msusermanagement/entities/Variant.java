package com.fujitsu.mmp.msusermanagement.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "variants")
public class Variant {
    @Id
    private String id;
    private String variantIdentifier;
    private String chromosomeSequence;
    private String initPosition;
    private String reference;
    private String alternative;
    private Boolean isChildren;
}
