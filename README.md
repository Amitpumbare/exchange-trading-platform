# Exchange-Style Trading Platform

A backend-authoritative exchange-style trading platform that replicates real exchange execution behavior with a deterministic in-memory matching engine, secure identity model, and production-grade backend architecture.

Unlike CRUD trading demos, this system implements a true matching engine as the execution authority, ensuring deterministic trade execution, concurrency safety, and immutable order lifecycle management.

---

# Project Objective

The goal of this project is to model real exchange trading behavior, focusing on:

- Deterministic price–time priority order matching
- Concurrency-safe execution
- Immutable order lifecycle
- Instrument-level engine isolation
- Secure identity model preventing enumeration attacks
- Backend as the single source of truth

The frontend acts only as a rendering and intent submission layer while the backend controls all execution logic.

---

# System Architecture

## Backend

- Spring Boot 3
- PostgreSQL (persistent storage)
- JPA / Hibernate (persistence layer)
- In-memory Matching Engine (execution layer)
- Redis (read caching layer)
- Spring Security + JWT Authentication
- Role-Based Access Control (RBAC)
- Spring Mail (password reset system)
- DTO-based API architecture
- AOP logging and monitoring

## Frontend

- Angular (Standalone Components)
- Reactive Forms
- JWT HTTP Interceptor
- Route Guards
- Toastr Notifications
- Execution-aware trading UI

The frontend never performs execution logic.

---

# Core Architecture Principle

### Backend as Single Source of Truth

Execution lifecycle:

```
Frontend → submits order intent
Matching Engine → executes deterministically
Database → persists execution facts
Redis → caches read models
Frontend → displays resulting state
```

Frontend never:

- calculates fills
- updates order status
- performs execution logic

This prevents:

- race conditions
- inconsistent state
- client-side manipulation

---

# Matching Engine Design

Each instrument has its own isolated matching engine instance.

```
ConcurrentHashMap<Long, OrderMatchingEngine>
```

Engines are lazily initialized using:

```
computeIfAbsent()
```

Benefits:

- instrument isolation
- parallel execution
- horizontal scalability
- reduced lock contention

---

# Order Book Data Structure

BUY Orders

- Max Heap (PriorityQueue)
- Highest price priority
- FIFO for same price

SELL Orders

- Min Heap (PriorityQueue)
- Lowest price priority
- FIFO for same price

Implements true exchange **price-time priority**.

---

# Heap Safety Rule

Objects inside a PriorityQueue are never mutated directly.

Incorrect approach:

```
modify object inside heap
```

Correct execution flow:

```
poll → modify → persist → reinsert
```

This preserves comparator consistency and heap ordering guarantees.

---

# Execution Capabilities

Supports:

- partial fills
- multi-order matching
- atomic lifecycle transitions
- deterministic execution
- price-time priority enforcement

Example:

```
BUY 100 @ 100
SELL 40 @ 90
SELL 60 @ 95
```

Result:

```
Trade 40
Trade 60
BUY order FILLED
```

---

# Concurrency Bug Identified and Solved

### Problem

Hibernate created different entity instances for the same order.

Example:

```
Heap contains: Order@A
Hibernate loads: Order@B
```

Since PriorityQueue.remove() uses equals(), removal failed.

Result:

- ghost orders
- duplicate execution risk
- heap corruption

### Solution

ID-based equality override.

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Order)) return false;
    Order other = (Order) o;
    return id != null && id.equals(other.id);
}

@Override
public int hashCode() {
    return id != null ? id.hashCode() : 0;
}
```

Result:

- reliable heap removal
- correct cancel-replace lifecycle
- concurrency-safe execution

---

# Immutable Order Lifecycle

Orders are never modified directly.

States:

```
OPEN
PARTIALLY_FILLED
FILLED
CANCELLED
```

Modify operation:

```
Cancel existing order
Create new order
Assign new ID
```

Benefits:

- deterministic execution
- auditability
- regulatory-style model

---

# Trades as Execution Facts

Trades represent execution results.

Trade entity contains:

- buyOrderId
- sellOrderId
- buyerUserId
- sellerUserId
- price
- quantity
- executedAt

Trades never change after creation.

```
Orders = Intent
Trades = Execution
```

---

# Secure Identity Model (UUID)

To prevent IDOR attacks, a dual identity model was implemented.

Instrument entity:

```
id (Long) → internal execution identity
publicId (UUID) → external identity
```

Frontend receives only:

- publicId
- symbol
- halted status

Backend resolves internally using:

```
findByPublicId(UUID)
```

---

# Authentication and Authorization

JWT-based authentication.

JWT contains:

- userId
- email
- role

Roles:

```
USER
ADMIN
```

Enforced using Spring Security with `@PreAuthorize`.

Users can access only their own orders and trades.

---

# Password Security

Passwords are stored using BCrypt hashing.

Passwords are never stored in plaintext.

---

# Secure Password Reset System

Implemented using UUID-based reset tokens.

Token entity contains:

- UUID token
- userId
- expiration timestamp

Flow:

```
User requests reset
Token generated
Stored in DB
Email sent via SMTP
User resets password
Token deleted
```

---

# DTO-Based API Layer

Entities are never exposed directly.

Example DTO:

```
TradeResponse
```

Prevents exposure of internal IDs and relationships.

---

# Eliminating N+1 Query Problem

Before:

```
Trade query
+ Order query per trade
+ Instrument query per trade
```

After:

```
Single JPQL projection query
```

Result:

```
1 query instead of hundreds
```

---

# Redis Caching

Implemented using:

```
@Cacheable
@CacheEvict
```

Caches:

- orders
- trades

Benefits:

- reduced DB load
- faster response times

---

# Security Hardening

After GitHub detected exposed SMTP credentials:

Actions taken:

- revoked compromised credential
- removed plaintext secrets
- externalized secrets using environment variables

Configuration now uses:

```
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
```

Repository is now safe to publish.

---

# Execution Flow

```
Frontend submits order
↓
OrderService validates request
↓
MatchingEngine executes
↓
Trades created
↓
Orders updated
↓
Persisted in PostgreSQL
↓
Cached in Redis
↓
DTO returned to frontend
```

---

# Production-Level Features

- Exchange-style matching engine
- Heap-based price-time priority
- Concurrency-safe order lifecycle
- Instrument-level engine isolation
- UUID-secure identity model
- DTO-based query optimization
- Redis caching layer
- JWT authentication
- RBAC authorization
- Secure email password reset
- Angular execution-aware frontend

---

# Future Enhancements

- WebSocket real-time order streaming
- Docker containerization
- Cloud deployment
- CI/CD pipeline
- Distributed matching engine
- Kafka event streaming

---

# Resume Summary

Designed and implemented an exchange-style trading platform using Spring Boot, PostgreSQL, Redis, and Angular, featuring a concurrency-safe in-memory matching engine with heap-based price-time priority execution, immutable order lifecycle, UUID-secured routing preventing IDOR attacks, Redis caching, DTO-based query optimization eliminating N+1 queries, and JWT-secured authentication with production-grade password reset via email.
