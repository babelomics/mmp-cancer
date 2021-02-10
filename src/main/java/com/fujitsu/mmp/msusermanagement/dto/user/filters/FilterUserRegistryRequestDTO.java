package com.fujitsu.mmp.msusermanagement.dto.user.filters;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterUserRegistryRequestDTO {
    private String firstName;
    private String lastName;
    private String identifier;
    private String organization;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date applicationDateStart;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date applicationDateEnd;
    private Boolean attended;
    private String search;
}
