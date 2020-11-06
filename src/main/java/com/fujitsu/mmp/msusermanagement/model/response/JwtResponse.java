package com.fujitsu.mmp.msusermanagement.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String id;
    private String identifier;
    private String email;
    private String userType;

    public JwtResponse(String accessToken, String id, String identifier, String email, String userType) {
        this.token = accessToken;
        this.id = id;
        this.identifier = identifier;
        this.email = email;
        this.userType = userType;
    }
}
