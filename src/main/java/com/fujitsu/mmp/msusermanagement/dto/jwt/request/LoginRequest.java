package com.fujitsu.mmp.msusermanagement.dto.jwt.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    private String identifier;
    private String password;
}
