package com.fujitsu.drugsapp.entities;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Table
@Entity
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DrugSet {

    @Id
    private UUID id;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updateAt = LocalDateTime.now();

    @OneToMany private List<Drug> drugs;
}
