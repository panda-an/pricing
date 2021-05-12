package com.lenovo.vro.pricing.configuration;

import com.lenovo.vro.pricing.service.WebUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final WebUserService webUserService;

    public LoginSuccessHandler(WebUserService webUserService) {
        this.webUserService = webUserService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        Map<String, String> map = webUserService.saveUserInfo((UserDetails) authentication.getPrincipal(), (WebAuthenticationDetails) authentication.getDetails());
        response.setHeader("region", map.get("region"));
        response.setHeader("id", map.get("id"));
        response.setHeader("Authorization", map.get("token"));
    }
}
