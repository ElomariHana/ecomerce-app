# API Gateway Setup Guide

## Overview
The API Gateway is the entry point for all client requests to the microservices. It handles routing, security, monitoring, and cross-cutting concerns.

## Architecture
- **Port**: 8085
- **Framework**: Spring Cloud Gateway
- **Service Discovery**: Eureka Client
- **Security**: Spring Security with OAuth2 support
- **Monitoring**: Spring Boot Actuator

## Features

### 1. **Service Routing**
Routes requests to appropriate microservices:
- `/api/products/**` → Product Service
- `/api/orders/**` → Order Service  
- `/api/users/**` → User Service
- `/api/shops/**` → Shop Service

### 2. **Security**
- OAuth2 Resource Server ready for Keycloak integration
- CORS configuration for cross-origin requests
- Rate limiting (configured for product service)

### 3. **Monitoring**
- Health checks via `/actuator/health`
- Gateway routes info via `/actuator/gateway`
- Request/response logging

### 4. **Load Balancing**
- Uses Eureka service discovery
- Automatic load balancing across service instances

## Configuration

### Application Properties
```yaml
server:
  port: 8085

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: product-service
          uri: lb://product
          predicates:
            - Path=/api/products/**
```

### Security Configuration
Currently configured to allow all API endpoints. For production:

1. **Enable OAuth2**:
   ```yaml
   spring:
     security:
       oauth2:
         resourceserver:
           jwt:
             issuer-uri: http://your-keycloak:8080/realms/ecommerce
   ```

2. **Secure specific endpoints**:
   ```java
   .pathMatchers("/api/admin/**").hasRole("ADMIN")
   .pathMatchers("/api/user/**").hasRole("USER")
   ```

## Docker Setup

### Dockerfile Features
- Multi-stage build for smaller image size
- Security: Non-root user
- Health checks
- Alpine Linux base for efficiency

### Build and Run
```bash
# Build the image
docker build -t api-gateway ./api-getway

# Run with docker-compose
docker-compose up api-gateway
```

## Testing

### Health Check
```bash
curl http://localhost:8085/actuator/health
```

### Gateway Routes
```bash
curl http://localhost:8085/actuator/gateway/routes
```

### Test Service Routing
```bash
# Product Service
curl http://localhost:8085/api/products/

# User Service  
curl http://localhost:8085/api/users/

# Order Service
curl http://localhost:8085/api/orders/
```

## Monitoring

### Logs
The gateway logs all requests and responses with timestamps:
```
[2024-01-15 10:30:45] GET /api/products/ from 192.168.1.100
[2024-01-15 10:30:45] GET /api/products/ -> 200
```

### Metrics
- Request count per route
- Response times
- Error rates
- Circuit breaker status

## Security Best Practices

### 1. **OAuth2 Integration**
```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        return http
            .oauth2ResourceServer(oauth2 -> oauth2.jwt())
            .authorizeExchange(authz -> authz
                .pathMatchers("/api/admin/**").hasRole("ADMIN")
                .anyExchange().authenticated()
            )
            .build();
    }
}
```

### 2. **Rate Limiting**
```yaml
filters:
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenishRate: 10
      redis-rate-limiter.burstCapacity: 20
```

### 3. **CORS Configuration**
```yaml
globalcors:
  corsConfigurations:
    '[/**]':
      allowedOrigins: "https://your-frontend.com"
      allowedMethods: "GET,POST,PUT,DELETE"
      allowedHeaders: "*"
```

## Troubleshooting

### Common Issues

1. **Service Discovery Issues**
   - Check Eureka dashboard: `http://localhost:8761`
   - Verify service registration

2. **Routing Issues**
   - Check gateway logs
   - Verify service names match Eureka registration

3. **Security Issues**
   - Check JWT token validity
   - Verify Keycloak configuration

### Debug Mode
Enable debug logging:
```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    org.springframework.web.reactive: DEBUG
```

## Production Considerations

1. **SSL/TLS**: Configure HTTPS
2. **Load Balancer**: Use external load balancer
3. **Monitoring**: Integrate with Prometheus/Grafana
4. **Logging**: Centralized logging (ELK stack)
5. **Backup**: Health check and circuit breakers
6. **Scaling**: Horizontal scaling with multiple instances 