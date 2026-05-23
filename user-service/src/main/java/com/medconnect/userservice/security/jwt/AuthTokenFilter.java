package com.medconnect.userservice.security.jwt;

import com.medconnect.userservice.security.session.AuthSessionService;
import com.medconnect.userservice.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthTokenFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthSessionService authSessionService;

    public AuthTokenFilter(
            JwtUtils jwtUtils,
            UserDetailsServiceImpl userDetailsService,
            AuthSessionService authSessionService
    ) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.authSessionService = authSessionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String sessionId = jwtUtils.getSessionIdFromJwtToken(jwt);
                if (StringUtils.hasText(sessionId)) {
                    if (!authSessionService.isSessionActive(sessionId)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired or revoked");
                        return;
                    }
                    authSessionService.touchSession(sessionId);
                }

                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (!userDetails.isEnabled()) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User account is disabled");
                    return;
                }
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            logger.warn("Authentication processing failed", e);
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }


    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
