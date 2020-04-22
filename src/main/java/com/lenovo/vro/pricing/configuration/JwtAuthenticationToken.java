package com.lenovo.vro.pricing.configuration;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private UserDetails userDetails;

    private String credentials;

    private final DecodedJWT token;


    public JwtAuthenticationToken(DecodedJWT decodedJWT) {
        super(Collections.emptyList());
        this.token = decodedJWT;
    }

    public JwtAuthenticationToken(UserDetails principal, DecodedJWT token, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.token = token;
        this.userDetails = principal;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return userDetails;
    }

    @Override
    public void setDetails(Object details) {
        super.setDetails(details);
        this.setAuthenticated(true);
    }

    public DecodedJWT getToken() {
        return token;
    }
}
