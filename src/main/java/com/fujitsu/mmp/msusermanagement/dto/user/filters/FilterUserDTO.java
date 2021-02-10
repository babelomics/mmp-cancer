package com.fujitsu.mmp.msusermanagement.dto.user.filters;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterUserDTO {
    private String firstName;
    private String lastName;
    private String identifier;
    private String email;
    private String organization;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date dateCreatedStart;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date dateCreatedEnd;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date dateLastAccessStart;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date dateLastAccessEnd;
    private String userType;
    private String search;
}
