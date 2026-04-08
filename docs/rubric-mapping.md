# Rubric Mapping (100-Point Checklist)

This document maps implementation evidence to each evaluation category for fast grading.

## 1) Service Decomposition (20)

Evidence:
- Independent services: `customer-service`, `restaurant-service`, `order-service`, `delivery-service`
- Dedicated DB containers and schemas:
  - `customer_db`, `restaurant_db`, `order_db`, `delivery_db`
- Cross-service joins replaced by IDs + APIs:
  - Order -> Customer/Restaurant via Feign
  - Delivery -> Order via Feign/internal endpoint

Key files:
- `order-service/src/main/java/.../client/*.java`
- `delivery-service/src/main/java/.../client/OrderClient.java`
- `docker-compose.yml`

## 2) Containerization (15)

Evidence:
- Multi-stage Dockerfiles for all services
- Root `docker-compose.yml` orchestrates all infrastructure + services
- Health checks and dependency ordering (`depends_on` with `service_healthy`)
- Docker profiles and env-based config per service

Key files:
- `docker-compose.yml`
- `*/Dockerfile`
- `*/src/main/resources/application-docker.yml`

## 3) Service Discovery & Gateway (20)

Evidence:
- Eureka server runs on `:8761`
- Services register/discover via logical names
- API Gateway routes business paths to services
- JWT validation filter at gateway
- Redis-backed rate limiting on hot endpoint (`POST /api/orders`)

Key files:
- `eureka-server/src/main/resources/application.properties`
- `api-gateway/api-gateway/src/main/java/.../GatewayConfig.java`
- `api-gateway/api-gateway/src/main/java/.../JwtAuthenticationFilter.java`
- `api-gateway/api-gateway/src/main/java/.../RateLimitingFilter.java`

## 4) Event-Driven Communication (15)

Evidence:
- RabbitMQ broker integrated
- `OrderPlacedEvent` and `OrderCancelledEvent` published by order-service
- Delivery consumes order events and creates/updates delivery assignments
- Delivery publishes delivery status event; order consumes status updates
- DLQ/DLX configured for failure handling
- Idempotency logic included for duplicate-safe processing

Key files:
- `order-service/src/main/java/.../event/*`
- `delivery-service/src/main/java/.../event/*`
- `order-service/src/main/java/.../config/RabbitMQConfig.java`
- `delivery-service/src/main/java/.../config/RabbitMQConfig.java`

## 5) Fault Tolerance (10)

Evidence:
- Resilience4j dependencies and properties configured
- Feign fallback factories implemented
- Graceful degraded behavior when dependencies fail
- Circuit breaker observability through actuator exposure

Key files:
- `*/pom.xml` (Resilience4j/Feign components)
- `*/src/main/resources/application.properties`
- `*/src/main/java/.../fallback/*FallbackFactory.java`

## 6) Code Quality & Documentation (20)

Evidence:
- Readable package structure and bounded context separation
- Architecture and sequence diagrams:
  - `docs/diagrams/architecture.drawio`
  - `docs/diagrams/order-delivery-events.drawio`
- Setup and troubleshooting documentation in `README.md`
- API contract reference in `docs/api-contracts.md`
- Migration rationale in README decision log
- CI workflow for tests:
  - `.github/workflows/ci.yml`

## Reviewer Quick Validation Steps

1. Start stack:
   - `docker compose up -d --build`
2. Check infrastructure:
   - Gateway `http://localhost:8080`
   - Eureka `http://localhost:8761`
   - RabbitMQ `http://localhost:15672`
3. Open Swagger via gateway:
   - `http://localhost:8080/swagger-ui.html`
4. Execute end-to-end flow:
   - register/login -> browse restaurants -> place order -> verify delivery assignment -> update delivery status -> verify order status update
5. Fault tolerance smoke test:
   - stop one downstream service, verify fallback/degraded response behavior
