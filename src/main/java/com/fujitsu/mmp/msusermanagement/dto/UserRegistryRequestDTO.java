package com.fujitsu.mmp.msusermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistryRequestDTO {
    private Long version;
    private String firstName;
    private String lastName;
    private String identifier;
    private String accessType;
    private String email;
    private String organization;
    private Date applicationDate;
    private String accessRequestReason;
    private String accessRefusalReason;
    private String userType;
    private String attended;
}
