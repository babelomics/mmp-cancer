package com.fujitsu.mmp.msusermanagement.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern="dd/MM/yyyy")
    private Date dateCreated;
    @JsonFormat(pattern="dd/MM/yyyy")
    private Date dateLastAccess;
    @JsonFormat(pattern="dd/MM/yyyy")
    private Date dateDeleted;
    private String userType;
}
