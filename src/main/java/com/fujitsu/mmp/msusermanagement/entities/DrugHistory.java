package com.fujitsu.mmp.msusermanagement.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.javamoney.moneta.Money;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "drugsHistory")
public class DrugHistory {
    @Id
    private String id;

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
