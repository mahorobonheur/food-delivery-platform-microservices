# Food Delivery Platform - Microservices (Spring Boot)

> [!TIP]
> Complexity: **Advanced**  
> Time Estimate: **14-18 hours**

## Cover

**Food Delivery Platform**  
**Microservices in Spring Boot**

Icon suggestion for submissions: `docs/diagrams/architecture.drawio` export (PNG/SVG).

## Overview

This project refactors a monolithic food delivery backend into independently deployable microservices using Spring Boot and Spring Cloud patterns.

The platform includes:

- `customer-service`
- `restaurant-service`
- `order-service`
- `delivery-service`
- `eureka-server`
- `api-gateway`

It uses:

- Eureka for service discovery
- Spring Cloud Gateway for unified API entry
- RabbitMQ for async inter-service communication
- OpenFeign for synchronous service-to-service calls
- Resilience4j for circuit breakers and fallbacks
- PostgreSQL databases (one per business service)
- Docker Compose for orchestration

## Target Architecture

- API Gateway (`:8080`) routes all client traffic
- Eureka Server (`:8761`) handles registration/discovery
- RabbitMQ (`:5672`) handles domain events
- Redis (`:6379`) supports gateway rate limiting
- Services + DBs:
  - Customer Service (`:8085`) -> `customer_db`
  - Restaurant Service (`:8086`) -> `restaurant_db`
  - Order Service (`:8088`) -> `order_db`
  - Delivery Service (`:8087`) -> `delivery_db`

See editable diagrams in:

- `docs/diagrams/architecture.drawio`
- `docs/diagrams/order-delivery-events.drawio`
- `docs/api-contracts.md`
- `docs/rubric-mapping.md`

## Service Boundaries and Responsibilities

### Customer Service

- Customer registration/login/profile
- Role management
- JWT issuance

### Restaurant Service

- Restaurant onboarding and updates
- Menu item management
- Menu and restaurant discovery APIs

### Order Service

- Order placement and order lifecycle
- Validates customer and restaurant/menu via Feign
- Publishes order events
- Consumes delivery status events and updates order status

### Delivery Service

- Consumes order placed/cancelled events
- Creates/updates delivery assignments
- Calls Order Service internal endpoint via Feign (with internal token)
- Publishes delivery status updates

## API Gateway

- Routes:
  - `/api/customers/**` -> customer-service
  - `/api/restaurants/**` -> restaurant-service
  - `/api/orders/**` -> order-service
  - `/api/deliveries/**` -> delivery-service
- JWT validation is enforced at gateway level
- Distributed Redis-backed rate limiting is applied to `POST /api/orders`

## Event-Driven Flows

### Order Placement

1. Client places order via Gateway.
2. Order Service stores order and publishes `OrderPlacedEvent`.
3. Delivery Service consumes event and creates delivery assignment.
4. Delivery Service publishes `DeliveryStatusUpdatedEvent` (modeled as `DeliveryStatusEvent`).
5. Order Service consumes delivery status and updates order state.

### Order Cancellation

1. Client cancels order via Gateway.
2. Order Service updates order to `CANCELLED` and publishes `OrderCancelledEvent`.
3. Delivery Service consumes cancel event and marks delivery as failed/cancelled.
4. Delivery Service publishes delivery status update.

### Message Reliability

- Dead letter queue is configured for failed order placed processing.
- Dead letter queue is configured for delivery status handling in order-service.
- Delivery assignment includes idempotency check by `orderId` to avoid duplicate deliveries.

## Security Model

- Gateway validates JWT for protected endpoints.
- Swagger and selected public endpoints are allow-listed.
- Internal service-to-service call (`delivery-service` -> `order-service` internal endpoint) uses `X-Internal-Token`.

Required environment variable:

- `INTERNAL_SERVICE_TOKEN` (same value in order-service and delivery-service)

## Fault Tolerance

- Feign circuit breakers enabled
- Fallback factories implemented for dependent service outages
- Resilience4j circuit breaker metrics exposed through actuator endpoints

## Default admin user (customer DB)

On **first** `postgres-customer` startup, scripts in `docker/postgres/customer/` create tables (if needed), seed roles, and insert an admin:

- **Email:** `admin@fooddelivery.test`
- **Password:** `password`

This only runs when the Postgres data volume is new. To re-run init, remove the `postgres_customer_data` volume and bring the stack up again.

## Run with Docker Compose

### Prerequisites

- Docker Desktop
- Docker Compose v2+

### 1) Create `.env` at repository root

Use values appropriate for your environment:

```env
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET_KEY=replace-with-strong-secret-of-sufficient-length
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
CLIENT_ID=your-google-client-id
CLIENT_SECRET=your-google-client-secret
INTERNAL_SERVICE_TOKEN=replace-with-shared-internal-token
INTERNAL_SERVICE_TOKENS=replace-with-shared-internal-token,optional-rotated-token
```

### 2) Start full system

```bash
docker compose up --build
```

### 3) Verify infrastructure

- Gateway: `http://localhost:8080`
- Eureka: `http://localhost:8761`
- RabbitMQ Mgmt: `http://localhost:15672`
- Swagger UI (gateway): `http://localhost:8080/swagger-ui.html`

## Local (Non-Docker) Run Order

1. Start PostgreSQL instances and RabbitMQ/Redis
2. Start `eureka-server`
3. Start `customer-service`
4. Start `restaurant-service`
5. Start `order-service`
6. Start `delivery-service`
7. Start `api-gateway`

## API Testing

Postman collection:

- `FOOD DELIVERY PLATFORM COLLECTION.postman_collection.json`

Recommended test path:

1. Register/login customer
2. Create/list restaurants and menu
3. Place order
4. Confirm delivery created automatically
5. Update delivery status and confirm order status propagation
6. Cancel order and confirm delivery cancel propagation
7. Stress `POST /api/orders` to validate rate limiting (`429`)

## Migration Decision Log (Condensed)

- Split by domain ownership: customer, restaurant, order, delivery.
- Replaced synchronous monolith delivery invocation with asynchronous events.
- Kept selective Feign calls where immediate consistency/validation is required.
- Applied gateway-edge auth for centralized access control.
- Added internal token for service-only endpoint security.
- Added distributed rate limiting (Redis) for hot endpoint protection.

## Migration Decision Log (Expanded)

1. **Boundary selection (Customer/Restaurant/Order/Delivery)**  
   Chosen by ownership of core data and business lifecycle, not by CRUD grouping. This minimizes cross-service write contention.

2. **Database-per-service**  
   Replaced monolith foreign keys with ID references and API/event contracts to enforce loose coupling and independent deployability.

3. **Hybrid communication style**  
   Used Feign for synchronous validation paths (order placement checks), and RabbitMQ events for asynchronous workflows (delivery assignment and status propagation).

4. **Gateway-edge security**  
   JWT validation at API gateway centralizes client auth, reduces duplicate edge auth logic, and keeps downstream services focused on authorization/business rules.

5. **Internal service auth**  
   Added internal token + client headers on service-only endpoint (`/api/orders/internal/**`) to separate user-facing and machine-to-machine trust.

6. **Reliability and operability**  
   Added circuit breakers/fallbacks for dependency outages, actuator endpoints for health/metrics, and DLQ/DLX for message failure isolation.

## Rubric Evidence

For direct grader mapping of all required criteria to implementation evidence, see:

- `docs/rubric-mapping.md`
- `docs/api-contracts.md`

## Troubleshooting

- `401` from internal order endpoint:
  - Ensure `INTERNAL_SERVICE_TOKEN` is set and identical for order + delivery.
- Swagger doc fetch `401`:
  - Verify gateway is running latest security + route config.
- Rabbit events not consumed:
  - Check RabbitMQ health and queue bindings.
- Gateway rate limiter not working:
  - Verify Redis is running and reachable by gateway.

## Notes

- Ports differ from some sample diagrams in assignment text. This implementation uses:
  - 8085, 8086, 8088, 8087 for business services
  - 8080 gateway
  - 8761 eureka

