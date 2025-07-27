package com.ecomerce.app.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenValidationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
            .map(context -> context.getAuthentication())
            .filter(authentication -> authentication instanceof JwtAuthenticationToken)
            .map(authentication -> (JwtAuthenticationToken) authentication)
            .map(jwtAuth -> {
                Jwt jwt = jwtAuth.getToken();
                ServerHttpRequest request = exchange.getRequest();
                
                String userId = extractUserId(jwt);
                String username = extractUsername(jwt);
                List<String> roles = extractRoles(jwt);
                
                ServerHttpRequest.Builder requestBuilder = request.mutate();
                
                if (userId != null) {
                    requestBuilder.header("X-User-ID", userId);
                }
                if (username != null) {
                    requestBuilder.header("X-Username", username);
                }
                if (roles != null && !roles.isEmpty()) {
                    requestBuilder.header("X-User-Roles", String.join(",", roles));
                }
                
                String token = extractTokenFromRequest(request);
                if (token != null) {
                    requestBuilder.header("X-JWT-Token", token);
                }
                
                log.debug("Added user context to request: userId={}, username={}, roles={}", 
                    userId, username, roles);
                
                return exchange.mutate().request(requestBuilder.build()).build();
            })
            .defaultIfEmpty(exchange)
            .flatMap(chain::filter);
    }

    private String extractUserId(Jwt jwt) {
        try {
            return jwt.getSubject(); // sub claim
        } catch (Exception e) {
            log.warn("Could not extract user ID from JWT", e);
            return null;
        }
    }

    private String extractUsername(Jwt jwt) {
        try {
            return jwt.getClaimAsString("preferred_username");
        } catch (Exception e) {
            try {
                return jwt.getClaimAsString("email");
            } catch (Exception ex) {
                log.warn("Could not extract username from JWT", ex);
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Jwt jwt) {
        try {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null) {
                return (List<String>) realmAccess.get("roles");
            }
        } catch (Exception e) {
            log.warn("Could not extract roles from JWT", e);
        }
        return null;
    }

    private String extractTokenFromRequest(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100; // Run after security filters
    }
} 