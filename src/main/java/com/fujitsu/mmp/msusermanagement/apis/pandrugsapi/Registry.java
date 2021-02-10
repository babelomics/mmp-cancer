package com.fujitsu.mmp.msusermanagement.apis.pandrugsapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Registry {
    private String login;
    private String password;
    private String email;
    private String uuid;
}
