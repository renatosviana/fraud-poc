# Fraud Detection POC (Java + Spring Boot + Redis + Postgres)

A minimal, event-driven fraud detection proof-of-concept using:
- Java 21
- Spring Boot 3
- Redis Streams
- PostgreSQL
- Docker Compose

## How to Run

### 1. Start infrastructure (Redis + Postgres + Grafana)

Running the Fraud POC Locally

This project uses Spring Boot, Redis Streams, PostgreSQL, and Grafana.
Below is the exact sequence to start the full pipeline end-to-end.

1. Start the infrastructure (Redis, Postgres, Grafana)
docker compose up -d


Verify containers:

docker ps


You should see:

redis-1

postgres-1

grafana-1

2. Run the Spring Boot application

In IntelliJ or terminal:

./gradlew bootRun


If successful, logs should include:

App started successfully
Detector started. Consuming from stream: tx

3. Run the transaction generator

In a second terminal (or another IntelliJ run config):

./gradlew runGenerator


This continuously pushes synthetic transactions into Redis Streams.

4. View the results in PostgreSQL

Connect to Postgres:

docker exec -it fraud-poc-postgres-1 psql -U poc -d poc


List tables:

\dt


Query recent alerts:

SELECT * FROM alerts ORDER BY created_at DESC LIMIT 20;


Result example:

| txn_id     | user_id     | reasons                          | score |
|------------|-------------|-----------------------------------|-------|
| unknownTxn | unknownUser | HIGH_AMOUNT;ANOMALY_ZSCORE; ...  |  1.0  |

5. Logs

Spring Boot bootRun logs show:

Startup

Redis consumption

Detected alerts

Errors/exceptions

Add your own logs with:

private static final Logger log = LoggerFactory.getLogger(App.class);
log.info("message here");

6. Project Structure (Hexagonal Architecture)
```
com.viana.poc
├── adapter/
│    ├── messaging/        (Redis Streams implementation)
│    └── persistence/      (Postgres via JdbcTemplate)
│
├── domain/
│    ├── model/            (Txn, business entities)
│    └── service/          (Detector business logic)
│
├── app/
│    └── App.java          (Application entrypoint)
│
└── resources/
├── application.yml
└── schema.sql
```

8. Architecture / Sequence Diagram

sequenceDiagram
autonumber

    participant Gen as TxnGenerator<br/>(domain)
    participant Bus as EventBus<br/>(port)
    participant Redis as RedisStreamsBus<br/>(adapter)
    participant Det as Detector<br/>(domain service)
    participant Repo as AlertRepo<br/>(DB adapter)
    participant DB as PostgreSQL

    rect rgb(240,240,240)
        note over Gen: Synthetic transaction generation loop
        Gen->>Gen: Create Txn (userId, amount, country, ...)
        Gen->>Bus: publish(txStream, txnJson)
        note right of Bus: At runtime, Bus is<br/>implemented by RedisStreamsBus
        Bus->>Redis: publish(txStream, txnJson)
        Redis->>Redis: XADD txStream { data: txnJson }
    end

    par Consumer loop
        note over Redis: Consumer group reading from txStream
        Redis->>Redis: XREADGROUP txStream for group/consumer
        Redis-->>Det: json = entry["data"]
    and Fraud detection
        note over Det: Domain logic – rules + stats
        Det->>Det: parse json → Txn
        Det->>Det: update running stats (per user)
        Det->>Det: compute score + reasons
        Det-->>Repo: if alert == true:<br/>save(txnId, userId, reasons, score)
    end

    rect rgb(240,240,240)
        note over Repo,DB: Persistence adapter
        Repo->>DB: INSERT INTO alerts(txn_id,user_id,reasons,score) VALUES (...)
        DB-->>Repo: success
        Repo-->>Det: (optional) ack
    end


9. Notes

Detector uses Micrometer for alert metrics.

Alerts are stored in PostgreSQL and also published to alertStream.

Detection uses simple rules + Z-score anomaly detection (rolling mean/std).