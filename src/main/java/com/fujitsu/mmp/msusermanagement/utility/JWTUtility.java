package com.fujitsu.mmp.msusermanagement.utility;

import com.fujitsu.mmp.msusermanagement.dto.UserDTO;
import com.fujitsu.mmp.msusermanagement.entities.Permission;
import com.fujitsu.mmp.msusermanagement.security.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
public class JWTUtility implements Serializable {

    private static final long serialVersionUID = 234234523523L;

    @Value("${jwt.expiration.time.seconds}")
    private long EXPIRATION_TIME;

    @Value("${jwt.expiration.time.link.seconds}")
    private long EXPIRATION_TIME_LINK;

    @Value("${jwt.secret}")
    private String secretKey;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;


    /**
     * Retrieve identifier from JWT token.
     * @param token
     * @return
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Retrieve expiration date from JWT token.
     * @param token
     * @return
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     *
     * @param token
     * @param claimsResolver
     * @param <T>
     * @return
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Get all claims from token. For retrieving any information from token we will need the secret key.
     * @param token
     * @return
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    /**
     * Check if the token has expired.
     * @param token
     * @return
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     *
     * Generate a token for an user.
     * @param userDetails
     * @return
     */
    public String generateToken(UserDetails userDetails, String userType, List<Permission> permissionList) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userType", userType);
        claims.put("permissionList", permissionList);
        return doGenerateToken(claims, userDetails.getUsername());
    }

    /**
     * Geneate a token for email link.
     * @param identifier
     * @return
     */
    public String generateTokenForLink(String identifier) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateTokenForLink(claims, identifier);
    }

    /**
     * Define claims of the token (expiration time, subject, issuer and the ID).
     * Sign the JWT using the HS256 algorithm and secret key.
     * @param claims
     * @param subject
     * @return
     */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME * 1000))
                .signWith(SignatureAlgorithm.HS256, secretKey).compact();
    }

    /**
     *
     * @param claims
     * @param subject
     * @return
     */
    private String doGenerateTokenForLink(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_LINK * 1000))
                .signWith(SignatureAlgorithm.HS256, secretKey).compact();
    }

    /**
     * Validate a token.
     * @param token
     * @param userDetails
     * @return
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     *
     * @param token
     * @return
     */
    public String refreshToken(String token) {

        long diffInMillis = Math.abs(getExpirationDateFromToken(token).getTime() - new Date().getTime());
        long diffInMinutes = TimeUnit.MINUTES.convert(diffInMillis, TimeUnit.MILLISECONDS);

        if (diffInMinutes < 10L) {
            String identifier = getUsernameFromToken(token);
            UserDetails userDetails
                    = userDetailsService.loadUserByUsername(identifier);

            Claims claims = getAllClaimsFromToken(token);

            return generateTokenFromClaims(userDetails, claims);
        }
        return token;
    }

    /**
     *
     * @param userDetails
     * @param claims
     * @return
     */
    private String generateTokenFromClaims(UserDetails userDetails, Claims claims) {
        return doGenerateToken(claims, userDetails.getUsername());
    }

}
