package com.fujitsu.mmp.msusermanagement.dto.jwt.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    private String identifier;
    private String password;
}
