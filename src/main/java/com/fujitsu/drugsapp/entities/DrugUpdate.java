package com.fujitsu.drugsapp.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Table
@Entity
@Getter
@Setter
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
