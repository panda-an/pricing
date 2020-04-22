package com.lenovo.vro.pricing.configuration;

import com.lenovo.vro.pricing.filter.MyUsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;

public class LoginConfigure<T extends LoginConfigure<T, B>, B extends HttpSecurityBuilder<B>> extends AbstractHttpConfigurer<T, B> {

    private final MyUsernamePasswordAuthenticationFilter authenticationFilter;

    public LoginConfigure() {
        this.authenticationFilter = new MyUsernamePasswordAuthenticationFilter();
    }

    @Override
    public void configure(B builder) throws Exception {
        authenticationFilter.setAuthenticationManager(builder.getSharedObject(AuthenticationManager.class));
        authenticationFilter.setAuthenticationFailureHandler(new LoginFailureHandler());
        authenticationFilter.setSessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy());

        MyUsernamePasswordAuthenticationFilter filter = postProcess(authenticationFilter);

        builder.addFilterAfter(filter, LogoutFilter.class);
    }

    public LoginConfigure<T, B> loginSuccessHandler(AuthenticationSuccessHandler handler) {
        authenticationFilter.setAuthenticationSuccessHandler(handler);
        return this;
    }
}
