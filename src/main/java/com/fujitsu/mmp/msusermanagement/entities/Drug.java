package com.fujitsu.mmp.msusermanagement.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "drugs")
public class Drug {
    @Id
    private String id;
    @Version
    private Long version;

    private String standardName;
    private String commonName;
    private List<AlternativeNames> alternativeNames;
    private Boolean available;
    private Double cost;
    private Date creationDate;
    private Date deletionDate;
    private String userId;
    private Long previousVersion;
}