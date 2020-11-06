package com.fujitsu.mmp.msusermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserHistoryDTO {
    private Long version;
    private String firstName;
    private String lastName;
    private String password;
    private String identifier;
    private String accessType;
    private String email;
    private String organization;
    private Date dateCreated;
    private Date dateLastAccess;
    private Date dateDeleted;
    private String userType;
}
