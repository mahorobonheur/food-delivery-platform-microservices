# API Contracts

This document provides reviewer-ready API contract coverage for all business services exposed through the API Gateway.

Base URL (gateway): `http://localhost:8080`

Authentication:
- Public endpoints: registration/login and swagger docs
- Protected endpoints: `Authorization: Bearer <jwt>`
- Internal service-only endpoint: `X-Internal-Token` + `X-Internal-Service`

## Customer Service

Gateway route prefix: `/api/customers`

- `POST /api/customers`
  - Purpose: Register customer
  - Auth: Public
  - Request: customer registration payload
  - Success: `201/200` with created customer/auth details (implementation-specific)
  - Errors: `400` validation, `409` duplicate email

- `POST /api/customers/login`
  - Purpose: Authenticate and issue JWT
  - Auth: Public
  - Request: email/password
  - Success: `200` with JWT token
  - Errors: `401` invalid credentials

- `GET /api/customers/{id}` (representative protected operation)
  - Purpose: Retrieve customer profile
  - Auth: JWT required
  - Success: `200`
  - Errors: `401`, `403`, `404`

## Restaurant Service

Gateway route prefix: `/api/restaurants`

- `GET /api/restaurants`
  - Purpose: Browse restaurants
  - Auth: JWT required
  - Success: `200` list

- `POST /api/restaurants`
  - Purpose: Create restaurant
  - Auth: JWT required (role-restricted by service security)
  - Success: `201/200`
  - Errors: `401`, `403`, `400`

- `GET /api/restaurants/{restaurantId}/menu`
  - Purpose: Browse menu items
  - Auth: JWT required
  - Success: `200`

## Order Service

Gateway route prefix: `/api/orders`

- `POST /api/orders`
  - Purpose: Place order
  - Auth: JWT required (`CUSTOMER`)
  - Behavior: validates customer/restaurant using Feign, publishes `OrderPlacedEvent`
  - Success: `201/200`
  - Errors: `400`, `401`, `403`, `404`, `503` (fallback/dependency outage)

- `GET /api/orders/{id}`
  - Purpose: Get order details/status
  - Auth: JWT required (owner or admin)
  - Success: `200`
  - Errors: `401`, `403`, `404`

- `PUT /api/orders/{id}/cancel` (path name may vary by implementation)
  - Purpose: Cancel order
  - Auth: JWT required (owner/admin policy)
  - Behavior: publishes `OrderCancelledEvent`
  - Success: `200`
  - Errors: `400`, `401`, `403`, `404`

- `GET /api/orders/internal/{id}`
  - Purpose: Internal read endpoint for delivery-service
  - Auth: Internal headers, not user JWT
  - Required headers:
    - `X-Internal-Token`
    - `X-Internal-Service: delivery-service`
  - Success: `200`
  - Errors: `401` invalid internal credentials

## Delivery Service

Gateway route prefix: `/api/deliveries`

- `GET /api/deliveries/{id}`
  - Purpose: Get delivery details
  - Auth: JWT required (`ADMIN` or `CUSTOMER` per service rules)
  - Success: `200`
  - Errors: `401`, `403`, `404`

- `PUT /api/deliveries/{id}/status` (path name may vary)
  - Purpose: Driver/admin status update
  - Auth: JWT required (role-restricted)
  - Behavior: publishes delivery status event consumed by order-service
  - Success: `200`
  - Errors: `400`, `401`, `403`, `404`

## OpenAPI/Swagger Coverage

Gateway Swagger definitions:
- `/customer-service/v3/api-docs`
- `/restaurant-service/v3/api-docs`
- `/order-service/v3/api-docs`
- `/delivery-service/v3/api-docs`

Swagger UI:
- `http://localhost:8080/swagger-ui.html`

Notes:
- OpenAPI in each service defines bearer auth (`bearer-jwt`) and relative server URL (`/`) for gateway-safe execution.
- Swagger UI is configured to persist auth across definition switches.
