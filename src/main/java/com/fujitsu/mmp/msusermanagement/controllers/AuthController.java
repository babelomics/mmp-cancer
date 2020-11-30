package com.fujitsu.mmp.msusermanagement.controllers;

import com.fujitsu.mmp.msusermanagement.model.request.LoginRequest;
import com.fujitsu.mmp.msusermanagement.model.request.SignupRequest;
import com.fujitsu.mmp.msusermanagement.model.response.JwtResponse;
import com.fujitsu.mmp.msusermanagement.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/auth")
@RestController
public class AuthController {
    @Autowired
    AuthService authService;

    /**
     * Get a token to access the system.
     * @param loginRequest: user identifier and password.
     * @return An object containing user information and a valid token to access the system.
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.signinUser(loginRequest);
    }

    /**
     * Set a password to a given user.
     * @param signUpRequest: user identifier and password.
     * @return Success message for valid identifier and password.
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signupUser(@Valid @RequestBody SignupRequest signUpRequest) {
        return authService.signupUser(signUpRequest);
    }

}
