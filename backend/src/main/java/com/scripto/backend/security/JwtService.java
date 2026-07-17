package com.scripto.backend.security;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.scripto.backend.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(User user){
        try{
            var algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("Scripto")
                    .withSubject(user.getEmail())
                    .withExpiresAt(expiresDate())
                    .sign(algorithm);
            return token;
        } catch (JWTCreationException exception){
            throw new RuntimeException("Error generating JWT", exception);
        }
    }

    public String validateToken(String tokenJWT){
        try{
            var algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("Scripto")
                    .build()
                    .verify(tokenJWT)
                    .getSubject();
        } catch (JWTVerificationException exception){
            throw new RuntimeException("Invalid or expired JWT token!");
        }
    }

    private Instant expiresDate(){
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }


    }


