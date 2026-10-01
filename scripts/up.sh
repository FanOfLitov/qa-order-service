#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(
    cd "$(dirname "${BASH_SOURCE[0]}")"
    pwd
)"

SYSTEM_DIR="$(
    cd "$SCRIPT_DIR/.."
    pwd
)"

ORDER_SERVICE_DIR="$SYSTEM_DIR/../qa-order-service"
AUDIT_SERVICE_DIR="$SYSTEM_DIR/../qa-order-audit-service"

echo
echo "======================================"
echo " Building Order Service"
echo "======================================"

cd "$ORDER_SERVICE_DIR"

./gradlew clean test bootJar --no-daemon


echo
echo "======================================"
echo " Building Audit Service"
echo "======================================"

cd "$AUDIT_SERVICE_DIR"

./gradlew clean test bootJar --no-daemon


echo
echo "======================================"
echo " Starting Docker system"
echo "======================================"

cd "$SYSTEM_DIR"

docker compose up -d --build


echo
echo "======================================"
echo " Containers"
echo "======================================"

docker compose ps


echo
echo "Waiting for Spring Boot services..."

wait_for_service() {

    local name="$1"
    local url="$2"

    for attempt in $(seq 1 30); do

        if curl -fsS "$url" > /dev/null; then

            echo "$name is UP"

            return 0
        fi

        echo "Waiting for $name... ($attempt/30)"

        sleep 2
    done

    echo "$name failed to start"

    return 1
}


wait_for_service \
    "order-service" \
    "http://localhost:8080/actuator/health"

wait_for_service \
    "audit-service" \
    "http://localhost:8081/actuator/health"


echo
echo "======================================"
echo " QA ORDER SYSTEM IS READY"
echo "======================================"
echo
echo "Order API:"
echo "http://localhost:8080/api/orders"
echo
echo "Audit API:"
echo "http://localhost:8081/api/audit-events"
echo