package com.kuit.findyou.global.jwt.util;

import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.global.jwt.exception.InvalidJwtException;
import com.kuit.findyou.global.jwt.exception.JwtExpiredException;
import com.kuit.findyou.global.jwt.exception.JwtNotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;

@Slf4j
@Component
public class JwtUtil {
    private final SecretKey secretKey;

    @Value("${findyou.jwt.access.expire-ms}")
    private long accessTokenExpireMs;

    public JwtUtil(@Value("${findyou.jwt.secret-key}") String secret) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public Long getUserId(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get(JwtClaimKey.USER_ID.getKey(), Long.class);
    }

    public Role getRole(String token) {
        String role = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get(JwtClaimKey.ROLE.getKey(), String.class);
        return Role.valueOf(role);
    }

    public JwtTokenType getTokenType(String token) {
        String tokenType = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get(JwtClaimKey.TOKEN_TYPE.getKey(), String.class);
        return JwtTokenType.valueOf(tokenType);

    }

    public String createAccessJwt(Long userId, Role role) {
        return Jwts.builder()
                .claim(JwtClaimKey.USER_ID.getKey(), userId)
                .claim(JwtClaimKey.ROLE.getKey(), role.name())
                .claim(JwtClaimKey.TOKEN_TYPE.getKey(), JwtTokenType.ACCESS_TOKEN)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpireMs))
                .signWith(secretKey)
                .compact();
    }

    public void validateJwt(String token){
        log.info("validateJwt");
        try{
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
        } catch (MalformedJwtException e) {
            throw new InvalidJwtException(INVALID_JWT);
        } catch (ExpiredJwtException e) {
            throw new JwtExpiredException(EXPIRED_JWT);
        } catch (UnsupportedJwtException e) {
            throw new InvalidJwtException(INVALID_JWT);
        } catch (IllegalArgumentException e) {
            throw new JwtNotFoundException(JWT_NOT_FOUND);
        }
    }
}