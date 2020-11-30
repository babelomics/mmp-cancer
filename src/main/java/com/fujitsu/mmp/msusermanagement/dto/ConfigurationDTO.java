package com.fujitsu.mmp.msusermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationDTO {
    private String contactIdentifier;
    private String contactName;
    private String contactLastName;
    private String contactEmail;
    private String setupInformation;
    private String pandrugURL;
    private String pandrugUser;
    private String pandrugPassword;
    private String pandrugEmail;
    private String cellbaseURL;
}


