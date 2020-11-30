package com.fujitsu.mmp.msusermanagement.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    private Long version;
    private String identifier;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private String accessType;
    private String organization;
    private Date dateCreated;
    private Date dateLastAccess;
    private String userType;
}
