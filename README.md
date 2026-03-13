# Exchange-Style Trading Platform

A backend-authoritative exchange-style trading platform that replicates real exchange execution behavior using a deterministic in-memory matching engine, secure identity model, real-time event streaming, and production-grade containerized infrastructure.

Unlike CRUD trading demos, this system implements a **true matching engine as the execution authority**, ensuring deterministic trade execution, concurrency safety, and immutable order lifecycle management.

---

# Project Objective

The goal of this project is to model real exchange trading behavior, focusing on:

* Deterministic price–time priority order matching
* Concurrency-safe execution
* Immutable order lifecycle
* Instrument-level engine isolation
* Secure identity model preventing enumeration attacks
* Backend as the single source of truth
* Real-time market event streaming
* Containerized infrastructure for cloud-ready deployment
* Automated CI pipeline validation

The frontend acts only as a rendering and intent submission layer while the backend controls all execution logic.

---

# System Architecture

## Backend

* Spring Boot 3
* PostgreSQL
* JPA / Hibernate
* In-memory Matching Engine
* Redis caching layer
* Spring Security + JWT authentication
* Role-Based Access Control (RBAC)
* Spring Mail password reset system
* DTO-based API architecture
* WebSocket + STOMP real-time messaging
* AOP logging

## Frontend

* Angular (Standalone Components)
* Reactive Forms
* JWT HTTP Interceptor
* Route Guards
* Toastr notifications
* Real-time UI updates via WebSockets

The frontend never performs execution logic.

---

# Core Architecture Principle

## Backend as Single Source of Truth

Execution lifecycle:

Frontend → submits order intent
Matching Engine → executes deterministically
Database → persists execution facts
Redis → caches read models
WebSocket → streams events
Frontend → renders state

The frontend never:

* calculates fills
* updates order status
* performs execution logic

This prevents:

* race conditions
* inconsistent order states
* client-side manipulation

---

# Matching Engine Design

Each instrument has its own isolated matching engine instance.

ConcurrentHashMap<Long, OrderMatchingEngine>

Engines are lazily initialized using:

computeIfAbsent()

Benefits:

* instrument isolation
* parallel execution
* reduced lock contention
* scalable architecture

---

# Order Book Data Structure

### BUY Orders

* Max Heap (PriorityQueue)
* Highest price priority
* FIFO for same price

### SELL Orders

* Min Heap (PriorityQueue)
* Lowest price priority
* FIFO for same price

This enforces the **price-time priority rule used by real exchanges**.

---

# Heap Safety Rule

Objects inside a PriorityQueue are **never mutated directly**.

Incorrect approach:

modify object inside heap

Correct lifecycle:

poll → modify → persist → reinsert

This preserves comparator consistency and heap ordering guarantees.

---

# Execution Capabilities

The matching engine supports:

* partial fills
* multi-order matching
* deterministic execution
* price-time priority enforcement
* atomic lifecycle transitions

Example execution:

BUY 100 @ 100
SELL 40 @ 90
SELL 60 @ 95

Result:

Trade 40
Trade 60
BUY order FILLED

---

# Concurrency Bug Identified and Solved

During development a critical issue was discovered.

Hibernate loads different entity instances for the same database row.

Example:

Heap contains → Order@A
Hibernate loads → Order@B

Both represent the same order but have different memory references.

Since PriorityQueue.remove() relies on equals(), removal failed.

This caused:

* ghost orders
* potential duplicate executions
* heap corruption

Solution:

Override equality using the database ID.

Result:

* reliable heap removal
* correct cancel-replace lifecycle
* concurrency-safe order book behavior

---

# Immutable Order Lifecycle

Orders are never modified directly.

States:

OPEN
PARTIALLY_FILLED
FILLED
CANCELLED

Modify operation:

Cancel existing order
Create new order
Assign new ID

Benefits:

* deterministic execution
* auditability
* regulatory-style order history

---

# Trades as Execution Facts

Trades represent immutable execution outcomes.

Trade entity contains:

* buyOrderId
* sellOrderId
* buyerUserId
* sellerUserId
* price
* quantity
* executedAt

Conceptually:

Orders = Intent
Trades = Execution

Trades never change after creation.

---

# Secure Identity Model

To prevent IDOR attacks, a dual identity model was implemented.

id (Long) → internal engine routing
publicId (UUID) → external API identity

Frontend receives only:

* publicId
* symbol
* halted status

Backend resolves internally using:

findByPublicId(UUID)

---

# Authentication & Authorization

JWT-based stateless authentication.

JWT token contains:

* userId
* email
* role

Roles:

USER
ADMIN

Authorization enforced using Spring Security with:

@PreAuthorize

Users can access only their own orders and trades.

---

# Password Security

Passwords stored using BCrypt hashing.

Passwords are never stored in plaintext.

---

# Secure Password Reset System

Password reset implemented using UUID-based tokens.

Token entity contains:

* token (UUID)
* userId
* expiration timestamp

Flow:

User requests reset
Token generated
Stored in DB
Email sent
User resets password
Token deleted

Security protections include:

* unguessable UUID tokens
* expiration limits
* single-use tokens

---

# DTO-Based API Layer

Entities are never exposed directly to clients.

Example:

TradeResponse DTO

Benefits:

* prevents internal ID exposure
* avoids circular entity relationships
* improves API performance

---

# Eliminating N+1 Query Problem

Initial implementation caused multiple database queries:

Trade query

* Order query per trade
* Instrument query per trade

This was optimized using JPQL DTO projection.

Result:

Single query replaces hundreds of queries.

---

# Redis Caching Layer

Caching implemented using Spring Cache abstraction.

Annotations used:

@Cacheable
@CacheEvict

Cached data:

* orders
* trades

Benefits:

* reduced database load
* faster API responses
* improved scalability

---

# Real-Time Event Streaming

Real-time updates implemented using:

Spring WebSocket
STOMP messaging

Events streamed:

/topic/orders/{userId}
/topic/trades/{userId}
/topic/depth/{instrumentId}

Execution pipeline:

Matching Engine
↓
Database Persistence
↓
Event Publisher
↓
WebSocket Broker
↓
Angular UI

---

# Infrastructure & DevOps

The trading platform is fully containerized and designed to mirror modern cloud deployment architectures.

---

# Docker Containerization

The backend runs inside a Docker container using a Java 17 runtime.

The Dockerfile:

* packages the Spring Boot JAR
* creates a runtime container
* exposes port 8081
* runs the application

Backend container endpoint:

localhost:8081

---

# Full Stack Containerization

The entire platform runs as a multi-container system using Docker Compose.

Services:

frontend → Angular served by Nginx
backend → Spring Boot application
postgres → PostgreSQL database
redis → Redis cache

System startup command:

docker compose up

Architecture:

Browser
↓
Angular Container (Nginx)
↓
Spring Boot Container
↓
PostgreSQL Container
↓
Redis Container

Docker networking allows containers to communicate using service names.

---

# Production Angular Container

The Angular frontend uses a **multi-stage Docker build**.

Stage 1 — Angular Build

Node.js compiles the Angular application and produces static build artifacts.

Stage 2 — Nginx Runtime

A lightweight Nginx container serves the compiled static files.

Benefits:

* faster startup
* smaller container size
* production-grade HTTP server

---

# SPA Routing Support

Angular uses client-side routing.

Refreshing routes such as:

/orders
/trades
/depth

would normally cause server 404 errors.

Nginx configuration resolves this:

try_files $uri $uri/ /index.html;

This ensures Angular Router handles route resolution correctly.

---

# Continuous Integration (CI)

A GitHub Actions pipeline verifies builds automatically.

Pipeline file:

.github/workflows/ci.yml

Pipeline flow:

Git push
↓
GitHub Actions runner
↓
Checkout repository
↓
Build backend (Maven)
↓
Build frontend (Angular)
↓
Build Docker images

This guarantees every commit produces deployable containers.

---

# Cloud Deployment Readiness

Because the system is containerized, it can be deployed to cloud infrastructure such as:

* AWS EC2
* DigitalOcean
* Render
* Fly.io

Containers ensure consistent execution across:

Local development
CI pipeline
Cloud servers

---

# Production-Level Features

* Exchange-style matching engine
* Heap-based price-time priority
* Concurrency-safe execution
* Immutable order lifecycle
* UUID-secure API routing
* DTO-based query optimization
* Redis caching layer
* WebSocket real-time streaming
* JWT authentication
* RBAC authorization
* Secure email password reset
* Dockerized full-stack architecture
* CI pipeline with automated container builds

---

# Future Enhancements

* automated Docker image publishing
* cloud deployment automation
* Kafka event streaming
* distributed matching engine
* advanced order book depth structures

---

# Resume Summary

Designed and implemented a backend-authoritative exchange-style trading platform using Spring Boot, PostgreSQL, Redis, and Angular, featuring a concurrency-safe in-memory matching engine with heap-based price-time priority execution, immutable order lifecycle, UUID-secured API routing preventing IDOR attacks, Redis caching, DTO-based query optimization eliminating N+1 queries, real-time WebSocket event streaming, and containerized deployment using Docker Compose with CI pipeline validation.
