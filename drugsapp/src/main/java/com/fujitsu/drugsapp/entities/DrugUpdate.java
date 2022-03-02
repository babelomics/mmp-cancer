package com.fujitsu.drugsapp.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Table
@Entity
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DrugUpdate {

    @Id
    private UUID id = UUID.randomUUID();

    @Column
    private UUID drugSetId;

    @Column
    private String description;

    @Column
    private UUID userId;

    @Column
    private UUID previousUpdateId;

    @Column
    private UUID nextUpdateId;

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

}