package com.ecommerce.authservice.configuration.security.jwt;

import com.ecommerce.authservice.configuration.security.service.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtils {

    /**
     * custom SHA_256 secretKey from config property.
     */
    @Value("${jwt.Secret}")
    private String jwtSecret;
    @Value("${jwt.ExpirationMs}")
    private int jwtExpirationMs;
    @Value("${jwt.refreshExpirationDateInMs}")
    private int jwtRefreshExpirationDateInMs;

    private static final String EXCEPTION = "exception";


    /**
     * Create token.
     *
     * @param authentication auth info
     * @return token
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        Map<String, Object> claims = new HashMap<>();
        Collection<? extends GrantedAuthority> roles = userPrincipal.getAuthorities();
        if (roles.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            claims.put("isAdmin", true);
        }
        if (roles.contains(new SimpleGrantedAuthority("ROLE_USER"))) {
            claims.put("isUser", true);
        }
        if (roles.contains(new SimpleGrantedAuthority("ROLE_MODERATOR"))) {
            claims.put("isModerator", true);
        }

        return Jwts.builder()
                .claims(claims)
                .subject((userPrincipal.getUsername()))
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Get username Information from jwt token.
     *
     * @param token token
     * @return auth info
     */
    public String getUserNameFromJwtToken(String token) {
        Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }


    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String doGenerateRefreshToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtRefreshExpirationDateInMs))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * validate token.
     *
     * @param authToken - jwt token
     * @return whether valid
     */
    public boolean validateJwtToken(String authToken, HttpServletRequest request) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (SecurityException ex) {
            request.setAttribute(EXCEPTION, ex);
            log.error("Invalid JWT signature trace: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            request.setAttribute(EXCEPTION, ex);
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("JWT token is expired: {}", ex.getMessage());

            String isRefreshToken = request.getHeader("isRefreshToken");
            String requestURL = request.getRequestURL().toString();
            // allow for Refresh Token creation if following conditions are true.
            if (isRefreshToken != null && isRefreshToken.equals("true") && requestURL.contains("refreshtoken")) {
                allowForRefreshToken(ex, request);
            } else{
                request.setAttribute(EXCEPTION, ex);
            }
        } catch (UnsupportedJwtException ex) {
            request.setAttribute(EXCEPTION, ex);
            log.error("JWT token is unsupported: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            request.setAttribute(EXCEPTION, ex);
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }


    /**
     * create a UsernamePasswordAuthenticationToken with null values. After setting the Authentication in the context,
     * we specify that the current user is authenticated.So it passes the Spring Security Configurations successfully.
     * Set the claims so that in controller we will be using it to create new JWT
     *
     * @param ex,request - jwt token
     */
    private void allowForRefreshToken(ExpiredJwtException ex, HttpServletRequest request) {
        var userAuthenticationToken = new UsernamePasswordAuthenticationToken(null, null, null);
        SecurityContextHolder.getContext().setAuthentication(userAuthenticationToken);
        request.setAttribute("claims", ex.getClaims());
    }
}
