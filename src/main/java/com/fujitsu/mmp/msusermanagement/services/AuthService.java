package com.fujitsu.mmp.msusermanagement.services;

import com.fujitsu.mmp.msusermanagement.entities.Permission;
import com.fujitsu.mmp.msusermanagement.entities.User;
import com.fujitsu.mmp.msusermanagement.dto.jwt.request.LoginRequest;
import com.fujitsu.mmp.msusermanagement.dto.jwt.request.SignupRequest;
import com.fujitsu.mmp.msusermanagement.dto.jwt.response.JwtResponse;
import com.fujitsu.mmp.msusermanagement.dto.jwt.response.MessageResponse;
import com.fujitsu.mmp.msusermanagement.repositories.PermissionRepository;
import com.fujitsu.mmp.msusermanagement.repositories.UserRepository;
import com.fujitsu.mmp.msusermanagement.security.UserDetailsImpl;
import com.fujitsu.mmp.msusermanagement.utility.JWTUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Service
public class AuthService {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JWTUtility jwtUtils;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    PasswordEncoder encoder;

    public ResponseEntity<?> signupUser (SignupRequest signupRequest) {

        User user = userRepository.findByIdentifier(signupRequest.getIdentifier());

        user.setPassword(encoder.encode(signupRequest.getPassword()));

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    public ResponseEntity<?> signinUser (LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getIdentifier(), loginRequest.getPassword()));

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findByIdentifier(loginRequest.getIdentifier());
        user.setDateLastAccess(new Date());
        userRepository.save(user);

        List<Permission> permissionList = permissionRepository.findByUserId(user.getId());

        String jwt = jwtUtils.generateToken(userDetails, user.getUserType(), permissionList, user.getFirstName(), user.getLastName());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                user.getUserType()
                ));
    }

    public ResponseEntity<?> refreshToken(String token, HttpServletResponse response) {
        String refreshedToken = jwtUtils.refreshToken(token, null);
        response.setHeader("Authorization", refreshedToken);
        return ResponseEntity.ok("");
    }
}