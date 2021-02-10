package com.fujitsu.mmp.msusermanagement.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "diagnosticPanelSet")
public class DiagnosticPanelSet {
    @Id
    private String id;
    private String diagnosticPanelSetIdentifier;
    private String name;
    private String description;
    private DiagnosticPanelReference reference;
    private String author;
    private Date creationDate;
    private Date deletionDate;
}
