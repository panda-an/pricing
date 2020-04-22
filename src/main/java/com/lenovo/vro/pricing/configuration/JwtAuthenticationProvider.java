package com.lenovo.vro.pricing.configuration;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.lenovo.vro.pricing.service.WebUserService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.www.NonceExpiredException;
import org.springframework.util.StringUtils;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final WebUserService webUserService;

    public JwtAuthenticationProvider(WebUserService webUserService) {
        this.webUserService = webUserService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        DecodedJWT jwtToken = ((JwtAuthenticationToken) authentication).getToken();

        if(jwtToken.getExpiresAt().before(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))) {
            throw new NonceExpiredException("Token expires");
        }

        String username = jwtToken.getSubject();

        if(StringUtils.isEmpty(username)) {
            throw new NonceExpiredException("Empty username");
        }

        UserDetails user = webUserService.getUserLoginInfo(username, jwtToken.getToken());

        if(user == null || StringUtils.isEmpty(user.getPassword())) {
            throw new NonceExpiredException("Token expires");
        }

        String salt = user.getPassword();
        if(StringUtils.isEmpty(salt)) {
            throw new BadCredentialsException("User salt is empty");
        }

        try {
            Algorithm algorithm = Algorithm.HMAC256(salt);
            JWTVerifier jwtVerifier = JWT.require(algorithm)
                    .withSubject(username)
                    .build();
            jwtVerifier.verify(jwtToken.getToken());
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadCredentialsException("JWT token verify failed", e);
        }

        return new JwtAuthenticationToken(user, jwtToken, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(JwtAuthenticationToken.class);
    }
}
