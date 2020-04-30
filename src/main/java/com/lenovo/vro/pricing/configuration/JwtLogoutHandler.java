package com.lenovo.vro.pricing.configuration;

import com.lenovo.vro.pricing.service.WebUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JwtLogoutHandler implements LogoutHandler {

    private final WebUserService webUserService;

    public JwtLogoutHandler(WebUserService webUserService) {
        this.webUserService = webUserService;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if(authentication == null) {
            return;
        }

        UserDetails userDetails = (UserDetails) authentication.getCredentials();
        if(userDetails != null && !StringUtils.isEmpty(userDetails.getUsername())) {
            webUserService.deleteUserInfo();
        }
    }
}
