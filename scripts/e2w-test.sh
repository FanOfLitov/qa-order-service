#!/usr/bin/env bash

set -e

ORDER_API="http://localhost:8080/api/orders"
AUDIT_API="http://localhost:8081/api/audit-events"

echo
echo "========================================"
echo " QA Order System E2E Test"
echo "========================================"
echo

echo "[1] Checking services..."

curl -fsS \
    http://localhost:8080/actuator/health \
    > /dev/null

curl -fsS \
    http://localhost:8081/actuator/health \
    > /dev/null

echo "Both services are UP"

echo
echo "[2] Creating order..."

CREATE_RESPONSE=$(
    curl -fsS \
        -X POST \
        "$ORDER_API" \
        -H "Content-Type: application/json" \
        -d '{
          "customerName":"E2E User",
          "product":"Test SSD",
          "quantity":1
        }'
)

echo "$CREATE_RESPONSE"

ORDER_ID=$(
    echo "$CREATE_RESPONSE" |
        python3 -c \
        'import json,sys; print(json.load(sys.stdin)["id"])'
)

echo
echo "Created order ID: $ORDER_ID"

echo
echo "[3] NEW -> PROCESSING"

curl -fsS \
    -X PATCH \
    "$ORDER_API/$ORDER_ID/status" \
    -H "Content-Type: application/json" \
    -d '{"status":"PROCESSING"}'

echo
echo

echo "[4] PROCESSING -> SHIPPED"

curl -fsS \
    -X PATCH \
    "$ORDER_API/$ORDER_ID/status" \
    -H "Content-Type: application/json" \
    -d '{"status":"SHIPPED"}'

echo
echo

echo "[5] SHIPPED -> DELIVERED"

curl -fsS \
    -X PATCH \
    "$ORDER_API/$ORDER_ID/status" \
    -H "Content-Type: application/json" \
    -d '{"status":"DELIVERED"}'

echo
echo

echo "[6] Waiting for Kafka consumer..."

sleep 2

echo
echo "[7] Audit history"

curl -fsS \
    "$AUDIT_API?orderId=$ORDER_ID"

echo
echo

echo "[8] Invalid transition DELIVERED -> CANCELLED"

HTTP_CODE=$(
    curl -s \
        -o /tmp/order-error.json \
        -w "%{http_code}" \
        -X PATCH \
        "$ORDER_API/$ORDER_ID/status" \
        -H "Content-Type: application/json" \
        -d '{"status":"CANCELLED"}'
)

echo "HTTP status: $HTTP_CODE"

cat /tmp/order-error.json

echo
echo

if [ "$HTTP_CODE" != "409" ]; then
    echo "FAILED: expected HTTP 409"
    exit 1
fi

echo
echo "========================================"
echo " E2E TEST PASSED"
echo "========================================"