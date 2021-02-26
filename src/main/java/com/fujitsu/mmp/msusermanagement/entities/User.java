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
@Document(collection = "users")
public class User {
    @Id
    private String id;
    @Version
    private Long version;
    private String identifier;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String accessType;
    private String organization;
    private Date dateCreated;
    private Date dateLastAccess;
    private String userType;
}
