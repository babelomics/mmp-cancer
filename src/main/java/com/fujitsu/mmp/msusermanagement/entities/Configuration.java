package com.fujitsu.mmp.msusermanagement.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "configurations")
public class Configuration {
    @Id
    private String id;
    private String contactIdentifier;
    private String contactName;
    private String contactLastName;
    private String contactEmail;
    private String setupInformation;
    private String pandrugURL;
    private String pandrugUser;
    private String pandrugEmail;
    private String pandrugPassword;
    private String genomicDictionaryURL;
}
