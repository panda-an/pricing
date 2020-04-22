package com.lenovo.vro.pricing.configuration;

import com.lenovo.vro.pricing.filter.JwtAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;

public class JwtAuthenticationConfiguration<T extends JwtAuthenticationConfiguration<T, B>, B extends HttpSecurityBuilder<B>> extends AbstractHttpConfigurer<T, B> {

    private final JwtAuthenticationFilter authenticationFilter;

    public JwtAuthenticationConfiguration() {
        this.authenticationFilter = new JwtAuthenticationFilter();
    }

    @Override
    public void configure(B builder) throws Exception {
        authenticationFilter.setAuthenticationManager(builder.getSharedObject(AuthenticationManager.class));
        authenticationFilter.setFailureHandler(new LoginFailureHandler());
        JwtAuthenticationFilter filter = postProcess(authenticationFilter);
        builder.addFilterBefore(filter, LogoutFilter.class);
    }

    public JwtAuthenticationConfiguration<T, B> permissiveRequestUrls(String... urls) {
        authenticationFilter.setPermissiveRequestMatchers(urls);
        return this;
    }

    public JwtAuthenticationConfiguration<T, B> tokenValidSuccessHandler(AuthenticationSuccessHandler authenticationSuccessHandler) {
        authenticationFilter.setSuccessHandler(authenticationSuccessHandler);
        return this;
    }
}
