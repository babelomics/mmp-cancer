package com.fujitsu.mmp.msusermanagement.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "usersHistory")
public class UserHistory {
    @Id
    private String id;
    @Version
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
