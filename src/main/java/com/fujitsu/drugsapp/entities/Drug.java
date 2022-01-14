package com.fujitsu.drugsapp.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Table
@Entity
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Drug {

    @Id
    private UUID id = UUID.randomUUID();

    @Column
    private UUID startUpdate;

    @Column
    private UUID endUpdate;

    @Column(nullable = false, unique = true)
    private String standardName;

    @Column(nullable = false, unique = true)
    private String commonName;

    @Column
    private UUID previousVersion;

    @Column
    private UUID nextVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "drugset_id", nullable = false)
    @JsonIgnore
    private DrugSet drugSet;

    @OneToMany(mappedBy = "drug", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<DrugName> drugNames = new ArrayList<>();
}
