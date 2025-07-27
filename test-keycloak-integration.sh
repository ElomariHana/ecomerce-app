#!/bin/bash

# Keycloak Integration Test Script
# This script tests the complete OAuth2/JWT flow with Keycloak and API Gateway

set -e

# Configuration
KEYCLOAK_URL="http://localhost:8080"
REALM="ecommerce"
CLIENT_ID="api-gateway"
CLIENT_SECRET="" # You need to set this from Keycloak admin console
GATEWAY_URL="http://localhost:8085"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}🔐 Keycloak Integration Test${NC}"
echo "=================================="

# Function to check if service is running
check_service() {
    local service_name=$1
    local url=$2
    echo -n "Checking $service_name... "
    if curl -s "$url" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Running${NC}"
        return 0
    else
        echo -e "${RED}✗ Not running${NC}"
        return 1
    fi
}

# Check if services are running
echo "Checking services..."
check_service "Keycloak" "$KEYCLOAK_URL" || exit 1
check_service "API Gateway" "$GATEWAY_URL/actuator/health" || exit 1

# Function to get access token
get_access_token() {
    local username=$1
    local password=$2
    
    if [ -z "$CLIENT_SECRET" ]; then
        echo -e "${RED}Error: CLIENT_SECRET is not set. Please get it from Keycloak admin console.${NC}"
        echo "1. Go to Keycloak Admin Console: $KEYCLOAK_URL"
        echo "2. Navigate to Clients → api-gateway → Credentials"
        echo "3. Copy the Secret value and set it in this script"
        exit 1
    fi
    
    echo -n "Getting access token for user '$username'... "
    
    local response=$(curl -s -X POST "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "grant_type=password" \
        -d "client_id=$CLIENT_ID" \
        -d "client_secret=$CLIENT_SECRET" \
        -d "username=$username" \
        -d "password=$password")
    
    local access_token=$(echo "$response" | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)
    
    if [ -n "$access_token" ]; then
        echo -e "${GREEN}✓ Success${NC}"
        echo "$access_token"
    else
        echo -e "${RED}✗ Failed${NC}"
        echo "Response: $response"
        return 1
    fi
}

# Function to test API endpoint
test_endpoint() {
    local endpoint=$1
    local token=$2
    local expected_status=$3
    local description=$4
    
    echo -n "Testing $description... "
    
    local response=$(curl -s -w "%{http_code}" -H "Authorization: Bearer $token" \
        "$GATEWAY_URL$endpoint")
    
    local status_code=$(echo "$response" | tail -c 4)
    local body=$(echo "$response" | head -c -4)
    
    if [ "$status_code" = "$expected_status" ]; then
        echo -e "${GREEN}✓ Success (Status: $status_code)${NC}"
    else
        echo -e "${RED}✗ Failed (Status: $status_code, Expected: $expected_status)${NC}"
        echo "Response: $body"
    fi
}

# Function to decode JWT token
decode_jwt() {
    local token=$1
    echo -e "${YELLOW}JWT Token Details:${NC}"
    echo "$token" | cut -d'.' -f2 | base64 -d 2>/dev/null | jq '.' 2>/dev/null || echo "Could not decode JWT"
    echo ""
}

# Main test flow
echo ""
echo -e "${YELLOW}📋 Testing OAuth2 Flow${NC}"
echo "========================"

# Get tokens for different users
echo "Getting access tokens..."

ADMIN_TOKEN=$(get_access_token "admin" "admin")
if [ $? -eq 0 ]; then
    echo -e "${GREEN}Admin token obtained${NC}"
    decode_jwt "$ADMIN_TOKEN"
fi

USER_TOKEN=$(get_access_token "user" "user")
if [ $? -eq 0 ]; then
    echo -e "${GREEN}User token obtained${NC}"
    decode_jwt "$USER_TOKEN"
fi

echo ""
echo -e "${YELLOW}🧪 Testing API Endpoints${NC}"
echo "=========================="

# Test public endpoints (should work without token)
echo "Testing public endpoints..."
test_endpoint "/actuator/health" "" "200" "Health check (public)"

# Test admin endpoints
echo ""
echo "Testing admin endpoints..."
test_endpoint "/api/products/admin/test" "$ADMIN_TOKEN" "200" "Admin products endpoint"
test_endpoint "/api/admin/test" "$ADMIN_TOKEN" "200" "Admin endpoint"

# Test user endpoints
echo ""
echo "Testing user endpoints..."
test_endpoint "/api/users/profile/test" "$USER_TOKEN" "200" "User profile endpoint"
test_endpoint "/api/orders/test" "$USER_TOKEN" "200" "User orders endpoint"

# Test unauthorized access
echo ""
echo "Testing unauthorized access..."
test_endpoint "/api/products/test" "" "401" "Products without token"
test_endpoint "/api/admin/test" "$USER_TOKEN" "403" "Admin endpoint with user token"

# Test token validation
echo ""
echo -e "${YELLOW}🔍 Testing Token Validation${NC}"
echo "============================="

# Test with invalid token
echo -n "Testing with invalid token... "
INVALID_RESPONSE=$(curl -s -w "%{http_code}" -H "Authorization: Bearer invalid.token.here" \
    "$GATEWAY_URL/api/products/test")
INVALID_STATUS=$(echo "$INVALID_RESPONSE" | tail -c 4)

if [ "$INVALID_STATUS" = "401" ]; then
    echo -e "${GREEN}✓ Correctly rejected invalid token${NC}"
else
    echo -e "${RED}✗ Should have rejected invalid token (Status: $INVALID_STATUS)${NC}"
fi

# Test with expired token (if you have one)
echo -n "Testing with expired token... "
echo -e "${YELLOW}⚠️  Manual test required${NC}"

echo ""
echo -e "${GREEN}✅ Keycloak Integration Test Complete${NC}"
echo "=========================================="
echo ""
echo -e "${YELLOW}Next Steps:${NC}"
echo "1. Check the logs for detailed information"
echo "2. Verify user context propagation in downstream services"
echo "3. Test with your actual frontend application"
echo "4. Configure proper CORS settings for production" 