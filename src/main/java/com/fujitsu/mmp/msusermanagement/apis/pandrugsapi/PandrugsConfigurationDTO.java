package com.fujitsu.mmp.msusermanagement.apis.pandrugsapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PandrugsConfigurationDTO {
    private String baseUrl;
    private String user;
    private String password;
    private String email;
}
