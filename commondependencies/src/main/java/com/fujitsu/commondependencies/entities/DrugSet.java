package com.fujitsu.commondependencies.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Table
@Entity
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DrugSet {

    @Schema(description = "Unique identifier of the DrugSet",
            example = "15473e48-7a0a-11ec-90d6-0242ac120003", required = true)
    @Id
    private UUID id = UUID.randomUUID();

    @Schema(description = "Name of the DrugSet",
            example = "Pandrug Set", required = true)
    @Column
    private String name;

    @Schema(description = "Description of the DrugSet",
            example = "DrugSet which contains the drugs from Pandrugs", required = true)
    @Column
    private String description;

    @Schema(description = "DateTime in which the DrugSet was created",
            example = "2022-01-20 12:15:38.253477", required = false)
    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

    @Schema(description = "DateTime in which the DrugSet was updated",
            example = "2022-01-20 12:15:38.253477", required = true)
    @Column
    private LocalDateTime updatedAt;

    @Schema(description = "List of associated drugs to the DrugSet",
            required = true)
    @OneToMany(mappedBy = "drugSet", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Drug> drugs;

}
