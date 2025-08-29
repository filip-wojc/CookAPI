package com.springtest.cookapi.infrastructure.security;

import com.springtest.cookapi.domain.responses.TokenPair;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JwtService {
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;
    @Value("${app.jwt.refresh-expiration}")
    private long refreshTokenExpirationMs;

    private static final String TOKEN_PREFIX = "Bearer ";


    //Generate access token
    public String generateAccessToken(Authentication authentication, Long userId) {
        Map<String, String> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("tokenType", "access");
        return generateToken(authentication, jwtExpirationMs, claims);
    }


    //Generate refresh token
    public String generateRefreshToken(Authentication authentication, Long userId) {

        Map<String, String> claims = new HashMap<>();
        claims.put("tokenType", "refresh");
        claims.put("userId", userId.toString());

        return generateToken(authentication, refreshTokenExpirationMs, claims);
    }
    //Validate token
    public boolean isValidToken(String token, UserDetails user) {
        final String username = extractUsernameFromToken(token);
        if (!username.equals(user.getUsername())) {
            return false;
        }
        try {
            Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
        }
        return false;
    }

    //Validate if the token is refresh token
    public boolean isValidRefreshToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return "refresh".equals(claims.get("tokenType"));
    }

    private String generateToken(Authentication authentication, long expirationTime, Map<String, String> claims) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);
        return Jwts.builder()
                .header()
                .add("typ", "JWT")
                .and()
                .subject(userPrincipal.getUsername())
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSignInKey())
                .compact();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public String extractUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public Long extractUserIdFromToken(String token) {
        var idClaim = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userId");
        return Long.valueOf(idClaim.toString());
    }

    public TokenPair generateTokenPair(Authentication authentication, Long userId) {
        String accessToken = generateAccessToken(authentication, userId);
        String refreshToken = generateRefreshToken(authentication, userId);
        return new TokenPair(accessToken, refreshToken);
    }
}