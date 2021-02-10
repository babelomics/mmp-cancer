package com.fujitsu.mmp.msusermanagement.apis.pandrugsapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginSession {
    private String username;
    private String password;
}

