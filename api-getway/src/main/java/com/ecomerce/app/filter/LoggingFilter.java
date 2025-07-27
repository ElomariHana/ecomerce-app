package com.ecomerce.app.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        
        String timestamp = LocalDateTime.now().format(formatter);
        String method = request.getMethod().name();
        String path = request.getPath().value();
        String remoteAddress = getRemoteAddress(request);
        String userAgent = request.getHeaders().getFirst(HttpHeaders.USER_AGENT);
        
        long startTime = System.currentTimeMillis();
        
        // Log request with user context
        return ReactiveSecurityContextHolder.getContext()
            .map(context -> context.getAuthentication())
            .filter(authentication -> authentication instanceof JwtAuthenticationToken)
            .map(authentication -> (JwtAuthenticationToken) authentication)
            .map(jwtAuth -> {
                String userId = jwtAuth.getToken().getSubject();
                String username = jwtAuth.getToken().getClaimAsString("preferred_username");
                log.info("[{}] {} {} from {} (User: {} - {}) - UserAgent: {}", 
                    timestamp, method, path, remoteAddress, userId, username, userAgent);
                return exchange;
            })
            .defaultIfEmpty(exchange)
            .flatMap(ex -> {
                log.info("[{}] {} {} from {} - UserAgent: {}", 
                    timestamp, method, path, remoteAddress, userAgent);
                return chain.filter(ex);
            })
            .then(Mono.fromRunnable(() -> {
                long duration = System.currentTimeMillis() - startTime;
                HttpStatus status = response.getStatusCode();
                String statusCode = status != null ? status.toString() : "unknown";
                
                log.info("[{}] {} {} -> {} ({}ms)", 
                    LocalDateTime.now().format(formatter), method, path, statusCode, duration);
            }));
    }

    private String getRemoteAddress(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null ? 
            request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}

