package com.example.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

/**
 * Utility class for generating and parsing JWT tokens.
 * The secret key and expiry duration are encapsulated here — never leak to other layers.
 */
public class JwtUtil {

    /** Token validity: 1 hour. */
    private static final long EXPIRY_MS = 1000L * 60 * 60;

    private static final String SECRET = "mysecretkeymysecretkeymysecretkey";
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    // Prevent instantiation — all methods are static utilities.
    private JwtUtil() {}

    /**
     * Generates a signed JWT with the username as the subject claim.
     *
     * @param username the authenticated user's username
     * @return compact JWT string
     */
    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRY_MS))
                .signWith(SIGNING_KEY)
                .compact();
    }

    /**
     * Validates and parses a JWT, returning the username stored in the subject claim.
     *
     * @param token compact JWT string
     * @return username
     * @throws JwtException if the token is invalid, expired, or tampered with
     */
    public static String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
