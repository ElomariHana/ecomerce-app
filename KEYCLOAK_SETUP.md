# Keycloak Integration Setup Guide

## Overview
This guide covers setting up Keycloak for OAuth2/JWT authentication with your API Gateway.

## 1. Keycloak Setup

### Start Keycloak
```bash
docker-compose up keycloak postgres
```

### Access Keycloak Admin Console
- URL: `http://localhost:8080`
- Username: `admin`
- Password: `admin`

## 2. Realm Configuration

### Create Realm
1. Click on the dropdown in the top-left corner
2. Click "Create Realm"
3. Name: `ecommerce`
4. Click "Create"

### Configure Realm Settings
1. Go to "Realm Settings" → "General"
2. Set "Display Name": `E-commerce Application`
3. Set "Realm Enabled": `ON`
4. Click "Save"

## 3. Client Configuration

### Create API Gateway Client
1. Go to "Clients" → "Create"
2. Client ID: `api-gateway`
3. Client Protocol: `openid-connect`
4. Click "Save"

### Configure Client Settings
1. **Settings Tab:**
   - Access Type: `confidential`
   - Valid Redirect URIs: `http://localhost:8085/*`
   - Web Origins: `http://localhost:8085`
   - Admin URL: `http://localhost:8085`

2. **Credentials Tab:**
   - Copy the "Secret" value (you'll need this for client authentication)

3. **Service Account Roles Tab:**
   - Click "Assign role"
   - Add roles: `admin`, `user`

## 4. User Management

### Create Users
1. Go to "Users" → "Add user"
2. Create users with different roles:

#### Admin User
- Username: `admin`
- Email: `admin@example.com`
- First Name: `Admin`
- Last Name: `User`
- Email Verified: `ON`

#### Regular User
- Username: `user`
- Email: `user@example.com`
- First Name: `Regular`
- Last Name: `User`
- Email Verified: `ON`

### Assign Roles
1. Click on the user → "Role Mappings"
2. Click "Assign role"
3. Add appropriate roles:
   - Admin user: `admin`, `user`
   - Regular user: `user`

## 5. Role Configuration

### Create Roles
1. Go to "Roles" → "Add Role"
2. Create roles:
   - `admin` (for administrative access)
   - `user` (for regular user access)

### Configure Role Mappings
1. Go to "Clients" → `api-gateway` → "Service Account Roles"
2. Assign roles to the service account

## 6. API Gateway Configuration

### Update application.yml
The API Gateway is already configured with Keycloak settings:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/ecommerce
          jwk-set-uri: http://localhost:8080/realms/ecommerce/protocol/openid-connect/certs
          audience: api-gateway
```

## 7. Testing the Integration

### Get Access Token
```bash
# Get token for admin user
curl -X POST http://localhost:8080/realms/ecommerce/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=api-gateway" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "username=admin" \
  -d "password=admin"
```

### Test Protected Endpoints
```bash
# Test with admin token
curl -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  http://localhost:8085/api/products/

# Test with user token
curl -H "Authorization: Bearer YOUR_USER_TOKEN" \
  http://localhost:8085/api/users/profile/
```

## 8. Security Features

### Role-Based Access Control
- **Admin Role**: Full access to all endpoints
- **User Role**: Limited access to user-specific endpoints
- **Public Endpoints**: Health checks and actuator endpoints

### JWT Token Validation
- Automatic token validation
- Role extraction from JWT claims
- User context propagation to downstream services

### Headers Added by Gateway
- `X-User-ID`: User's unique identifier
- `X-Username`: User's username
- `X-User-Roles`: Comma-separated list of roles
- `X-JWT-Token`: Original JWT token

## 9. Troubleshooting

### Common Issues

1. **Token Validation Fails**
   - Check Keycloak realm name matches configuration
   - Verify client secret is correct
   - Ensure JWT audience matches

2. **Role-Based Access Denied**
   - Verify roles are assigned to users
   - Check role names match security configuration
   - Ensure roles are included in JWT token

3. **CORS Issues**
   - Check Keycloak client configuration
   - Verify allowed origins in API Gateway

### Debug Logging
Enable debug logging in application.yml:
```yaml
logging:
  level:
    org.springframework.security: DEBUG
    com.ecomerce.app.filter: DEBUG
```

## 10. Production Considerations

### Security Hardening
1. Use HTTPS for all communications
2. Implement proper CORS policies
3. Add rate limiting
4. Use secure client secrets
5. Implement token refresh logic

### Monitoring
1. Monitor JWT token validation failures
2. Track authentication attempts
3. Log security events
4. Monitor user session activity

### Scaling
1. Use Keycloak clustering
2. Implement Redis for session storage
3. Use load balancers for high availability
4. Consider using Keycloak Operator for Kubernetes 