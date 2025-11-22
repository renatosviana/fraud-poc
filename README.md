# Fraud Detection POC (Java + Spring Boot + Redis + Postgres)

A minimal, event-driven fraud detection proof-of-concept using:
- Java 21
- Spring Boot 3
- Redis Streams
- PostgreSQL
- Docker Compose

## How to Run

### 1. Start infrastructure (Redis + Postgres + Grafana)

```bash
docker compose up -d
