# ddd-base-project

Template project for Zenvite2 microservices. Clone and rename to bootstrap a new service with clean architecture,
domain events, and infrastructure pre-configured.

## Tech Stack

| Component    | Version                              |
|--------------|--------------------------------------|
| Java         | 21                                   |
| Maven        | via mvnw                             |
| Spring Boot  | 3.5.13                               |
| MariaDB      | via Docker                           |
| Kafka        | KRaft cluster (3 brokers) via Docker |
| Redis        | via Docker                           |
| vds-platform | 1.0.0-SNAPSHOT (DDD building blocks) |

## Project Structure

```
ddd-base-project/
├── domain/              # Pure Java — aggregates, value objects, events, repository interfaces
├── application/         # Use case orchestration — ports, commands, results
├── adapter/             # Spring Boot app — controllers, JPA, Kafka, config
│   └── src/main/resources/
│       ├── application.yml
│       └── db/migration/        # Flyway SQL migrations
└── docker/
    └── docker-compose.yml       # MariaDB, Redis, Kafka, Kafka UI
```

**Dependency rule:** `domain ← application ← adapter` — inner layers never import outer.

## Prerequisites

- Java 21+
- Docker & Docker Compose
- Access to VDS Nexus (credentials in `~/.m2/settings.xml`)

## Quick Start

```bash
# 1. Start infrastructure
cd docker && docker compose up -d && cd ..

# 2. Build
./mvnw clean install -DskipTests

# 3. Run
./mvnw spring-boot:run -pl adapter

# 4. Verify
curl http://localhost:8088/actuator/health
```

## Infrastructure

Start all dependencies before building or running tests:

```bash
cd docker && docker compose up -d
```

| Service  | Container      | Host Port           |
|----------|----------------|---------------------|
| MariaDB  | base-mariadb   | 13306               |
| Redis    | base-redis     | 16379               |
| Kafka    | base-kafka-1/2/3 | 19092, 19093, 19094 |
| Kafka UI | base-kafka-ui  | 18080               |

> Ports use `1` prefix (13306 instead of 3306) to avoid conflicts with other services on the dev machine.

Health checks:

```bash
docker exec base-mariadb mariadb -ubaseapp -pbaseapp -e "SELECT 1"
docker exec base-redis redis-cli ping
docker ps --filter name=base-kafka
```

## Build & Test

```bash
# Build without tests
./mvnw clean install -DskipTests

# Unit tests (no Docker needed)
./mvnw test -pl domain                           # Domain aggregate logic
./mvnw test -pl application                      # UseCase tests (mocked repos)

# Architecture tests (no Docker needed)
./mvnw test -pl adapter -Dtest='ArchitectureTest'

# Integration tests (Docker required)
./mvnw test -pl adapter -Dtest='*IntegrationTest'

# Full build (unit + integration, Docker required)
./mvnw clean install
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

Kafka topic format: `{app}.{boundedContext}.{eventName}` (e.g. `base.order.confirmed`)

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
    "publishedBy": "base-service"
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

1. Copy `ddd-base-project/` → `{service-name}/`
2. Update root `pom.xml`: change `artifactId` and `name`
3. Update `application.yml`: rename `spring.application.name`
4. Rename package `com.zenvite2.base` → `com.zenvite2.{service}`
5. Rename `BaseApplication` → `{Service}Application`
6. Study the sample code — note file locations, class names, and how layers connect
7. Remove sample code (grep `TODO: Remove this sample code`)
8. Add Flyway migrations in `adapter/src/main/resources/db/migration/`
9. Implement domain aggregates, use cases, and adapters

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

| Property                         | Default                                  | Description        |
|----------------------------------|------------------------------------------|--------------------|
| `server.port`                    | 8088                                     | HTTP port          |
| `spring.datasource.url`          | `jdbc:mariadb://localhost:13306/basedb`  | MariaDB connection |
| `spring.kafka.bootstrap-servers` | `localhost:19092,...`                    | Kafka brokers      |
| `spring.data.redis.host`         | `localhost`                              | Redis host         |
| `spring.data.redis.port`         | 16379                                    | Redis port         |
