# ntbh-base-project

Template project for NTBH Platform microservices. Clone and rename to bootstrap a new service with clean architecture,
domain events, and infrastructure pre-configured.

## Tech Stack

| Component    | Version                              |
|--------------|--------------------------------------|
| Java         | 21                                   |
| Gradle       | 8.14 (Kotlin DSL)                    |
| Spring Boot  | 3.4.9                                |
| MariaDB      | via Docker                           |
| Kafka        | KRaft cluster (3 brokers) via Docker |
| Redis        | via Docker                           |
| vds-platform | 1.0.0-SNAPSHOT (DDD building blocks) |

## Project Structure

```
ntbh-base-project/
├── domain/              # Pure Java — aggregates, value objects, events, repository interfaces
├── application/         # Use case orchestration — ports, commands, results
├── adapter/             # Spring Boot app — controllers, JPA, Kafka, config
│   └── src/main/resources/
│       ├── application.yml
│       └── db/migration/        # Flyway SQL migrations
├── docker/
│   └── docker-compose.yml       # MariaDB, Redis, Kafka, Kafka UI
└── build.gradle.kts             # Root build with shared config
```

**Dependency rule:** `domain ← application ← adapter` — inner layers never import outer.

## Prerequisites

- Java 21+
- Docker & Docker Compose
- Access to VDS Nexus (credentials in `gradle.properties`)

## Quick Start

```bash
# 1. Start infrastructure
cd docker && docker compose up -d && cd ..

# 2. Build
./gradlew build -x test

# 3. Run
./gradlew :adapter:bootRun

# 4. Verify
curl http://localhost:8088/actuator/health
```

## Infrastructure

Start all dependencies before building or running tests:

```bash
cd docker && docker compose up -d
```

| Service  | Container        | Host Port           |
|----------|------------------|---------------------|
| MariaDB  | ntbh-mariadb     | 13306               |
| Redis    | ntbh-redis       | 16379               |
| Kafka    | ntbh-kafka-1/2/3 | 19092, 19093, 19094 |
| Kafka UI | ntbh-kafka-ui    | 18080               |

> Ports use `1` prefix (13306 instead of 3306) to avoid conflicts with other services on the dev machine.

Health checks:

```bash
docker exec ntbh-mariadb mariadb -untbh -pntbh -e "SELECT 1"
docker exec ntbh-redis redis-cli ping
docker ps --filter name=ntbh-kafka
```

## Build & Test

```bash
# Build without tests
./gradlew build -x test

# Unit tests (no Docker needed)
./gradlew :domain:test           # Domain aggregate logic
./gradlew :application:test      # UseCase tests (mocked repos)

# Architecture tests (no Docker needed)
./gradlew :adapter:test --tests '*ArchitectureTest'

# Integration tests (Docker required)
./gradlew :adapter:test --tests '*IntegrationTest'

# Full build (unit + integration, Docker required)
./gradlew build
```

## Architecture

### Request Flow

```
Controller → Facade → Port (inbound) → UseCase → Domain → Repository (outbound) → JPA
```

- **UseCase** is NOT a Spring bean — wired via `@Configuration @Bean` in adapter
- **Facade** maps DTOs and delegates to ports — no business logic
- **Scheduler/Kafka consumer** also routes through Facade

### Domain Event Flow

```
UseCase (@EventPublishHandler)
  → DomainEventPublisher.publish(event)          [ThreadLocal]
  → EventPublishingProxy                          [after method return]
    → LoggingEventHandler                         [always]
    → KafkaEventHandler                           [when Kafka available]
```

Events extend `AbstractDomainEvent(DomainEventType, aggregateType, DTime)` and auto-generate `eventId` (UUID v7).

Kafka topic format: `ntbh.{boundedContext}.{eventName}` (e.g. `ntbh.order.confirmed`)

### Kafka Event Envelope

All events published to Kafka follow the standard envelope format:

```json
{
  "eventId": "uuid-v7",
  "eventType": "order.confirmed",
  "eventVersion": "1.0",
  "occurredAt": "2026-03-26T10:00:00+07:00",
  "tenantId": null,
  "aggregateId": "order-uuid",
  "aggregateType": "Order",
  "payload": {
    ...
  },
  "metadata": {
    "correlationId": null,
    "causationId": null,
    "publishedBy": "ntbh-base-service"
  }
}
```

### ArchUnit Rules

`ArchitectureTest` enforces:

- Domain module has no Spring/JPA imports
- UseCase is not annotated with `@Service`/`@Component`
- Controller/Scheduler must go through Facade
- Inbound adapters don't directly access outbound adapters

Violations fail the build.

## Creating a New Service

1. Copy `ntbh-base-project/` → `ntbh-{service-name}/`
2. Update `settings.gradle.kts`: change `rootProject.name`
3. Rename package `ntbh.base` → `ntbh.{service}`
4. Rename `BaseApplication` → `{Service}Application`
5. Remove all sample code (grep for `TODO: Remove this sample code`)
6. Add Flyway migrations in `adapter/src/main/resources/db/migration/`
7. Implement domain aggregates, use cases, and adapters

## Key Dependencies

| Library                   | Purpose                                                                             |
|---------------------------|-------------------------------------------------------------------------------------|
| `vds-platform:spring-web` | DDD building blocks (Aggregate, Identity, Repository, DomainEvent, EventPublishing) |
| MapStruct                 | DTO ↔ Domain mapping                                                                |
| ArchUnit                  | Architecture rule enforcement                                                       |
| REST Assured              | API integration testing                                                             |
| Flyway                    | Database migrations                                                                 |

## Configuration

Application config: `adapter/src/main/resources/application.yml`

| Property                         | Default                               | Description        |
|----------------------------------|---------------------------------------|--------------------|
| `server.port`                    | 8088                                  | HTTP port          |
| `spring.datasource.url`          | `jdbc:mariadb://localhost:13306/ntbh` | MariaDB connection |
| `spring.kafka.bootstrap-servers` | `localhost:19092,...`                 | Kafka brokers      |
| `spring.data.redis.host`         | `localhost`                           | Redis host         |
| `spring.data.redis.port`         | 16379                                 | Redis port         |
