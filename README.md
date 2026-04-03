# 🚀 Exchange Trading Platform — Engineering Summary

A **backend-authoritative exchange-style trading platform** that simulates real-world exchange execution using a deterministic in-memory matching engine, real-time event streaming, and production-grade cloud deployment.

Unlike CRUD-based trading apps, this system implements a **true matching engine as the execution authority**, ensuring deterministic execution, concurrency safety, and immutable order lifecycle management.

---

## 🎯 Core Architecture

The system follows a strict **backend-as-single-source-of-truth model**:

Frontend → Order Intent  
Matching Engine → Execution  
Database → Persistence  
Redis → Caching  
WebSocket → Event Streaming  
Frontend → Rendering Only  

The frontend never performs execution logic, preventing race conditions and inconsistent state.

---

## ⚙️ Matching Engine

* In-memory engine per instrument using `ConcurrentHashMap`
* BUY → Max Heap (highest price priority)
* SELL → Min Heap (lowest price priority)
* FIFO for same price (price-time priority)

### Capabilities

* Deterministic matching  
* Partial fills  
* Multi-order execution  
* Concurrency-safe lifecycle  
* Atomic execution flow  

---

## 🔁 Self-Trade Prevention (STP)

* Detects same user on both sides of trade  
* Prevents self-matching  
* Maintains order book integrity  

Ensures compliance with real exchange behavior and prevents artificial volume manipulation.

---

## 🧾 Order & Trade Model

### Immutable Order Lifecycle

OPEN → PARTIALLY_FILLED → FILLED → CANCELLED  

* Orders are never modified  
* Modify = cancel + recreate  

### Trades as Execution Facts

* Immutable records of execution  
* Orders = intent  
* Trades = outcome  

---

## 🧠 Execution Tracking Model (New Enhancement)

Introduced a **dual-field order state model**:

* `quantity` → original order size (immutable)  
* `executedQuantity` → filled quantity (progress tracker)  

### Derived State

remaining = quantity - executedQuantity  

### Benefits

* Eliminates data loss from mutation-based design  
* Enables accurate partial fill tracking  
* Fixes UI inconsistencies (0 quantity bug)  
* Supports reliable real-time event streaming  

---

## ⚡ Real-Time Event Interpretation Layer

Implemented a frontend **event interpretation system**:

* Backend emits **state transitions**  
* Frontend interprets them as **user actions**

Example:

CANCELLED → OPEN (within time window) → MODIFY  

### Improvements

* Eliminated duplicate toasts  
* Removed ghost events from ReplaySubject  
* Stabilized event stream using Subject  
* Added time-based correlation for MODIFY detection  
* Introduced delayed cancel handling  

---

## 📡 Real-Time System

* WebSocket + STOMP messaging  
* User-scoped topics:

/topic/orders/{userId}  
/topic/trades/{userId}  
/topic/depth/{instrumentId}  

### Enhancements

* Fixed PARTIALLY_FILLED event handling  
* Prevented event stream break due to malformed payloads  
* Ensured stable multi-user concurrent execution  
* Added REST fallback sync for UI consistency  

---

## 🎯 UI & Real-Time UX Improvements

* Accurate partial fill display:

executedQuantity / quantity  

* Correct toast behavior:

  * Place → single toast  
  * Modify → single toast  
  * Cancel → delayed toast  
  * Partial → precise execution update  
  * Fill → exact execution quantity  

* Eliminated need for tab-switch refresh  
* Fixed toast system breakdown issues  

---

## ⚡ Performance Optimizations

* Redis caching (`@Cacheable`, `@CacheEvict`)  
* DTO projections to eliminate N+1 queries  
* Constant-time market summary (best bid/ask)  
* Efficient heap-based execution  

---

## 🔐 Security Model

* JWT-based stateless authentication  
* RBAC authorization (USER / ADMIN)  
* UUID-based public IDs (prevents IDOR attacks)  
* BCrypt password hashing  
* Secure password reset (UUID + expiry)  
* Secrets externalized via environment variables  

---

## 🐳 Infrastructure & DevOps

### Containerized Architecture

* Frontend → Angular + Nginx  
* Backend → Spring Boot  
* Database → PostgreSQL  
* Cache → Redis  

Managed via Docker Compose:

Browser  
↓  
Nginx (Reverse Proxy)  
↓  
Frontend Container  
↓  
Backend Container  
↓  
PostgreSQL + Redis  

---

### 🌐 Reverse Proxy (Nginx)

* Single entry point (port 80)  
* Routes:

/ → Frontend  
/api → Backend  
/ws → WebSocket  

✔ Backend hidden  
✔ Reduced attack surface  
✔ Production-ready architecture  

---

### ⚙️ CI/CD Pipeline

Implemented using GitHub Actions:

* Build backend (Maven)  
* Build frontend (Angular)  
* Build Docker images  
* Validate deployment readiness  

---

### ☁️ Cloud Deployment

Deployed on AWS EC2 using Docker Compose:

* Container-based runtime  
* Persistent PostgreSQL storage  
* Redis caching layer  
* Public access via EC2 IP  

---

## 🏆 Key Features

* Exchange-style matching engine  
* Price-time priority execution  
* Self-trade prevention (STP)  
* Concurrency-safe design  
* Immutable order lifecycle  
* Execution tracking with executedQuantity  
* Redis caching layer  
* Real-time WebSocket streaming  
* Event interpretation layer  
* Stable multi-user execution handling  
* JWT authentication & RBAC  
* Secure password reset system  
* Full-stack Docker containerization  
* CI pipeline validation  
* AWS cloud deployment  
* Nginx reverse proxy architecture  

---

## 🎯 Resume Summary

Designed and deployed an exchange-style trading platform using Spring Boot, PostgreSQL, Redis, and Angular, featuring a concurrency-safe in-memory matching engine with price-time priority and self-trade prevention (STP), immutable order lifecycle, execution tracking via executedQuantity, DTO-based query optimization eliminating N+1 issues, Redis caching, real-time WebSocket event streaming with frontend event interpretation, and production-grade deployment using Docker, CI/CD pipelines, and AWS EC2 with Nginx reverse proxy.
