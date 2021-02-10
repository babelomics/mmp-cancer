package com.fujitsu.mmp.msusermanagement.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "regions")
public class Region {
    @Id
    private String id;
    private String regionIdentifier;
    private String chromosomeSequence;
    private String initPosition;
    private String endPosition;
}
