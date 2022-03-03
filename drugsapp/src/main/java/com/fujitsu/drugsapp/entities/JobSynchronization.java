package com.fujitsu.drugsapp.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Table
@Entity
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobSynchronization {

    @Id
    private UUID id = UUID.randomUUID();

    private UUID drugsetId;

    private String status;

}
