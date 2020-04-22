package com.lenovo.vro.pricing.filter;

import com.lenovo.vro.pricing.entity.SystemUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StreamUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MyUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public MyUsernamePasswordAuthenticationFilter() {
        super(new AntPathRequestMatcher("/login", HttpMethod.POST.name()));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws AuthenticationException, IOException, ServletException {
        String body = StreamUtils.copyToString(httpServletRequest.getInputStream(), StandardCharsets.UTF_8);
        ObjectMapper objectMapper =new ObjectMapper();
        SystemUser user = objectMapper.readValue(body, SystemUser.class);

        return this.getAuthenticationManager().authenticate(
                new UsernamePasswordAuthenticationToken(user.getUserName()==null?"":user.getUserName().trim(), user.getPassword()));
    }
}
