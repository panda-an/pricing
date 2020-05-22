package com.lenovo.vro.pricing.configuration;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.lenovo.vro.pricing.service.WebUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

public class JwtRefreshSuccessHandler implements AuthenticationSuccessHandler {

    private static final int tokenRefreshInterval = 300;

    private final WebUserService webUserService;

    public JwtRefreshSuccessHandler(WebUserService webUserService) {
        this.webUserService = webUserService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        DecodedJWT jwtToken = ((JwtAuthenticationToken) authentication).getToken();
        if(shouldTokenRefresh(jwtToken.getExpiresAt())) {
            Map<String, String> map = webUserService.saveUserInfo((UserDetails) authentication.getPrincipal());
            response.setHeader("Authorization", map.get("token"));
        }
    }

    protected boolean shouldTokenRefresh(Date issueAt){
        LocalDateTime issueTime = LocalDateTime.ofInstant(issueAt.toInstant(), ZoneId.systemDefault());
        return LocalDateTime.now().plusSeconds(tokenRefreshInterval).isAfter(issueTime);
    }
}
