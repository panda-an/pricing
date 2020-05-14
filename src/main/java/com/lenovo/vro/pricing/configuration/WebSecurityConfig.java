package com.lenovo.vro.pricing.configuration;

import com.lenovo.vro.pricing.filter.OptionsRequestFilter;
import com.lenovo.vro.pricing.service.WebUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.header.Header;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private WebUserService webUserService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/system/**").hasRole("ADMIN")
                .anyRequest().permitAll()
            .and()
            .csrf().disable()
            .sessionManagement().disable()
            .formLogin().disable()
            .cors()
            .and()
            .headers().addHeaderWriter(new StaticHeadersWriter(Arrays.asList(
                    new Header("Access-control-Allow-Origin", "*"),
                    new Header("Access-Control-Expose-Headers", "Authorization"))))
            .and()
            .addFilterAfter(new OptionsRequestFilter(), CorsFilter.class)
            .apply(new LoginConfigure<>()).loginSuccessHandler(loginSuccessHandler())
            .and()
            .apply(new JwtAuthenticationConfiguration<>()).tokenValidSuccessHandler(jwtRefreshSuccessHandler());

    }

    @Bean
    protected LoginSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler(webUserService);
    }

    @Bean
    protected JwtRefreshSuccessHandler jwtRefreshSuccessHandler() {
        return new JwtRefreshSuccessHandler(webUserService);
    }

/*    @Bean
    protected JwtLogoutHandler jwtLogoutHandler() {
        return new JwtLogoutHandler(webUserService);
    }*/

    @Bean("jwtAuthenticationProvider")
    protected AuthenticationProvider  jwtAuthenticationProvider() {
        return new JwtAuthenticationProvider(webUserService);
    }

    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(daoAuthenticationProvider()).authenticationProvider(jwtAuthenticationProvider());
    }

    @Bean("daoAuthenticationProvider")
    protected AuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();
        daoProvider.setUserDetailsService(webUserService);
        daoProvider.setPasswordEncoder(new BCryptPasswordEncoder());
        return daoProvider;
    }

    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "HEAD", "OPTION"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.addExposedHeader("Authorization");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
