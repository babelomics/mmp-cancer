package com.fujitsu.drugsapp.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Table
@Entity
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Drug {

    @Schema(description = "Unique identifier of the Drug",
            example = "15473e48-7a0a-11ec-90d6-0242ac120003", required = true)
    @Id
    private UUID id = UUID.randomUUID();

    @Schema(description = "Identifier of the start update of the Drug",
            example = "15473e48-7a0a-11ec-90d6-0242ac120003", required = true)
    @Column
    private UUID startUpdate;

    @Schema(description = "Identifier of the end update of the Drug",
            example = "15473e48-7a0a-11ec-90d6-0242ac120003", required = true)
    @Column
    private UUID endUpdate;

    @Schema(description = "Standard name of the Drug",
            example = "Bicuculine", required = true)
    @Column(nullable = false)
    private String standardName;

    @Schema(description = "Common name of the Drug",
            example = "Bicuculine", required = true)
    @Column(nullable = false, unique = true)
    private String commonName;

    @Schema(description = "Identifier of the previous version of the Drug",
            example = "15473e48-7a0a-11ec-90d6-0242ac120003", required = true)
    @Column
    private UUID previousVersion;

    @Schema(description = "Identifier of the next version of the Drug",
            example = "15473e48-7a0a-11ec-90d6-0242ac120003", required = true)
    @Column
    private UUID nextVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "drugset_id", nullable = false)
    @JsonIgnore
    private DrugSet drugSet;

    @Schema(description = "List of associated drugNames to the Drug",
            required = true)
    @OneToMany(mappedBy = "drug", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<DrugName> drugNames = new ArrayList<>();
}
