//package com.scripto.backend.security;
//
//
//import com.auth0.jwt.JWT;
//import com.auth0.jwt.algorithms.Algorithm;
//import com.auth0.jwt.exceptions.JWTCreationException;
//import com.scripto.backend.user.entity.User;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import javax.crypto.SecretKey;
//
//@Service
//public class JwtService {
//
//    @Value("${jwt.secret}") //add no aplication.properties
//    private String secret;
//
//    public String generateToken(User user){
//        try{
//            var algorithm = Algorithm.HMAC256(secret);
//            String token = JWT.create()
//                    .withIssuer("Scripto")
//                    .withSubject(user.getEmail())
//                    .withExpiresAt(ExpiresDate)
//                    .sign(algorithm);
//            return token;
//        } catch (JWTCreationException exception){
//            throw new RuntimeException("Error generating JWT", exception);
//        }
//    }
//
//    public String getSubject(String tokenJWT){
//        try{
//            var algorithm =
//        }
//    }
//    ){
//
//    }
//
//
//}
