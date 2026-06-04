package com.medconnect.userservice.security;

import com.medconnect.userservice.security.jwt.AuthEntryPointJwt;
import com.medconnect.userservice.security.jwt.AuthTokenFilter;
import com.medconnect.userservice.security.jwt.JwtUtils;
import com.medconnect.userservice.security.session.AuthSessionService;
import com.medconnect.userservice.security.services.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    private final AuthEntryPointJwt unauthorizedHandler;

    public WebSecurityConfig(AuthEntryPointJwt unauthorizedHandler) {
        this.unauthorizedHandler = unauthorizedHandler;
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter(
            JwtUtils jwtUtils,
            UserDetailsServiceImpl userDetailsService,
            AuthSessionService authSessionService
    ) {
        return new AuthTokenFilter(jwtUtils, userDetailsService, authSessionService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthTokenFilter authTokenFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/signup",
                                "/api/auth/register",
                                "/api/auth/verify-email",
                                "/api/auth/resend-otp",
                                "/api/auth/signin",
                                "/api/auth/login",
                                "/api/auth/verify-login",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/google",
                                "/api/auth/refresh"
                        ).permitAll()
                        .requestMatchers("/api/auth/logout", "/api/auth/sessions/**", "/api/auth/mfa/**").authenticated()
                        .requestMatchers(
                                "/api/users/me",
                                "/api/users/patients/**",
                                "/api/users/doctors",
                                "/api/users/doctors/*",
                                "/api/users/doctors/search",
                                "/api/users/pharmacists",
                                "/api/users/pharmacists/*",
                                "/api/users/professional-documents/**",
                                "/api/users/search",
                                "/api/users/*/subscription",
                                "/api/users/batch-import/**",
                                "/api/users/*/clinics",
                                "/api/users/clinics/**"
                        ).authenticated()
                        .requestMatchers(
                                "/api/users",
                                "/api/users/doctors/*/verification",
                                "/api/users/pharmacists/*/verification"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                );

        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
