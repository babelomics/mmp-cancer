package com.fujitsu.drugsapp.entities;

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
    private UUID sourceId = UUID.randomUUID();

    @Column
    private UUID drugId;

    @Column
    private String name;

    @ManyToOne
    private Drug drug;
}
