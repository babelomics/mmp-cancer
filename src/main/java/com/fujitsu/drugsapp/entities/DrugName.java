package com.fujitsu.drugsapp.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Table
@Entity
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DrugName {
    @Id
    private UUID id = UUID.randomUUID();

    @Column
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "drug_id", nullable = false)
    @JsonIgnore
    private Drug drug;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "drugSource_id", nullable = false)
    @JsonIgnoreProperties("drugNames")
    private DrugSource drugSource;

}
