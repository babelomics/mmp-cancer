package com.fujitsu.drugsapp.entities;

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
    private UUID uuid = UUID.randomUUID();

    @Column
    private UUID startUpdate = UUID.randomUUID();

    @Column
    private UUID endUpdate = UUID.randomUUID();

    @Column
    private String standardName;

    @Column
    private String commonName;

    @Column
    private UUID previousVersion = UUID.randomUUID();

    @Column
    private UUID nextVersion;

    @ManyToOne
    private DrugSet drugSet;

    @OneToMany
    private List<DrugName> drugNames = new ArrayList<>();

    @OneToMany
    private List<DrugSource> drugSources = new ArrayList<>();
}