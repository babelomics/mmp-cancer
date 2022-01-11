package com.fujitsu.drugsapp.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Table
@Entity
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DrugSource {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable=false, unique=true)
    private String shortName;

    @Column
    private URL url;

    @OneToMany(mappedBy = "drugSource", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<DrugName> drugNames;
}
