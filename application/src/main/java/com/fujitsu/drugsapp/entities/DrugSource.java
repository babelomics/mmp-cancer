package com.fujitsu.drugsapp.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.net.URL;
import java.util.List;
import java.util.UUID;

@Table
@Entity
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DrugSource {

    @Schema(description = "Unique identifier of the DrugSource",
            example = "15473e48-7a0a-11ec-90d6-0242ac120003", required = true)
    @Id
    private UUID id = UUID.randomUUID();

    @Schema(description = "Name of the DrugSource",
            example = "DrugBank", required = true)
    @Column(nullable=false, unique=true)
    private String shortName;

    @Schema(description = "URL to the DrugSource",
            example = "https://go.drugbank.com/", required = false)
    @Column
    private URL url;

    @OneToMany(mappedBy = "drugSource", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<DrugName> drugNames;
}
