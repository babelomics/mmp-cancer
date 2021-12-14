package com.drugsapp.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Table
@Entity
@Getter
@Setter
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
