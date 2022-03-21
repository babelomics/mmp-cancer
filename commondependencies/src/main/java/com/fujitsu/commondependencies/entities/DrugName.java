package com.fujitsu.commondependencies.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

@Table
@Entity
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DrugName {

    @Schema(description = "Unique identifier of the DrugName",
            example = "15473e48-7a0a-11ec-90d6-0242ac120003", required = true)
    @Id
    private UUID id = UUID.randomUUID();

    @Schema(description = "Name of the DrugName",
            example = "Bicuculine", required = true)
    @Column
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "drug_id", nullable = false)
    @JsonIgnore
    private Drug drug;

    @Schema(description = "List of associated drugSources to the DrugName",
            required = true)
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "drugSource_id", nullable = false)
    @JsonIgnoreProperties("drugNames")
    private DrugSource drugSource;

}
