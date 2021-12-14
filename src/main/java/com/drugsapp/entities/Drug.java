package com.drugsapp.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Table
@Entity
@Getter
@Setter
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
    private List<DrugName> drugNames;

    @OneToMany
    private List<DrugSource> drugSources;
}
