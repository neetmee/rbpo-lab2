package com.rbpo.rbpo_lab2.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

public class JwtTokenFilter extends OncePerRequestFilter {

    private static final Set<String> AUTH_POST_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/auth/login",
            "/auth/register",
            "/auth/refresh"
    );

    private final JwtTokenProvider jwtTokenProvider;
    private final AppUserDetailsService userDetailsService;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;

    public JwtTokenFilter(
            JwtTokenProvider jwtTokenProvider,
            AppUserDetailsService userDetailsService,
            JsonAuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && AUTH_POST_PATHS.contains(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String token = resolveBearerToken(request);
            if (token != null) {
                JwtTokenProvider.ParsedToken parsed = jwtTokenProvider.parseAccess(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(parsed.subject());

                if (!userDetails.isEnabled()) {
                    throw new DisabledException("User account is disabled");
                }
                if (!userDetails.isAccountNonLocked()) {
                    throw new LockedException("User account is locked");
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            }
        } catch (JwtTokenProvider.InvalidJwtException | IllegalArgumentException | AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            AuthenticationException authEx = (ex instanceof AuthenticationException ae)
                    ? ae
                    : new BadCredentialsException("Invalid or expired JWT access token", ex);
            authenticationEntryPoint.commence(request, response, authEx);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null) {
            return null;
        }
        if (!header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new BadCredentialsException("Expected Bearer token");
        }
        String token = header.substring(7).trim();
        if (token.isEmpty()) {
            throw new BadCredentialsException("Bearer token is empty");
        }
        return token;
    }
}