package com.food.delivery.order_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class InternalServiceAuthFilter extends OncePerRequestFilter {

    private final Set<String> acceptedTokens;
    private final Set<String> allowedClients;

    public InternalServiceAuthFilter(
            @Value("${internal.service.tokens:${internal.service.token}}") String configuredTokens,
            @Value("${internal.service.allowed-clients:delivery-service}") String configuredClients) {
        this.acceptedTokens = Arrays.stream(configuredTokens.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
        this.allowedClients = Arrays.stream(configuredClients.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getServletPath().startsWith("/api/orders/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("X-Internal-Token");
        String client = request.getHeader("X-Internal-Service");

        boolean tokenValid = token != null && acceptedTokens.contains(token);
        boolean clientValid = client != null && allowedClients.contains(client);
        if (!tokenValid || !clientValid) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid internal service token\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
