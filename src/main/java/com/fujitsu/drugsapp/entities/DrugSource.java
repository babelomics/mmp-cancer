package com.fujitsu.drugsapp.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.net.URL;
import java.util.UUID;

@Table
@Entity
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DrugSource {

    @Id
    private UUID id = UUID.randomUUID();

    @Column
    private String shortName;

    @Column
    private String name;

    @Column
    private URL url;

    @ManyToOne
    private Drug drug;
}
