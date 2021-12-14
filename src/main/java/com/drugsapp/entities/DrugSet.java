package com.drugsapp.entities;
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
public class DrugSet {

    @Id
    private UUID id = UUID.randomUUID();

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updateAt;

    @OneToMany
    private List<Drug> drugs;
}
