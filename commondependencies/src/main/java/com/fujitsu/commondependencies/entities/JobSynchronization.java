package com.fujitsu.commondependencies.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobSynchronization {

    @Id
    private UUID id = UUID.randomUUID();

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

    private String drugsetName;

    private String status;

}