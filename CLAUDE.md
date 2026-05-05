# CLAUDE.md

Template project for Zenvite2 microservices. Clone and rename to create a new service.

## Architecture

- **UseCase is NOT a Spring bean** — instantiated via `@Configuration @Bean` in adapter
- **Request flow:** Controller → Facade → Port → UseCase (never skip Facade)
- **Scheduler/Kafka consumer** also routes through Facade, same as Controller
- **ArchUnit** (`ArchitectureTest`) enforces all above — violations fail build
- **Repository queries** — prefer `findBy(Criteria)` over custom methods; add custom only for multi-join, aggregation, or performance-critical paths

## Domain Event Flow

```
UseCase (@EventPublishHandler) → DomainEventPublisher.publish() [ThreadLocal]
  → EventPublishingProxy [after method return] → DomainEventHandler.handle()
    → LoggingEventHandler (always) + KafkaEventHandler (when Kafka available)
```

- Events extend `AbstractDomainEvent(DomainEventType, aggregateType, DTime)` — auto-generates `eventId` (UUID v7)
- `@EventPublishHandler` on UseCase method is **required** for events to dispatch
- Kafka topic: `{app}.{boundedContext}.{eventName}` — e.g. `base.order.confirmed`
- Kafka envelope: includes eventId, eventType, eventVersion, tenantId, aggregateId, aggregateType, payload, metadata (correlationId, causationId, publishedBy)
- Consumer must call Facade, never UseCase directly

## Pitfalls

- **JPA import in domain** → compiles but ArchUnit catches it
- **`@Service`/`@Component` on UseCase** → ArchUnit fails
- **Docker not running** → `Connection refused`. Check `docker ps`, don't retry build
- **Port prefix `1`** → 13306 (MariaDB), 16379 (Redis), 19092-19094 (Kafka) to avoid conflicts
- **Kafka/Redis in tests** → `@ConditionalOnProperty` excludes them, no impact

## Infrastructure

```bash
cd docker && docker compose up -d    # MariaDB:13306, Redis:16379, Kafka:19092-94, KafkaUI:18080
```

## Build & Test

```bash
./mvnw clean install -DskipTests                                          # Build only
./mvnw spring-boot:run -pl adapter                                        # Run locally
./mvnw test -pl domain                                                    # Domain unit tests (no Docker)
./mvnw test -pl application                                               # UseCase tests (no Docker)
./mvnw test -pl adapter -Dtest='ArchitectureTest'                         # ArchUnit rules (no Docker)
./mvnw test -pl adapter -Dtest='*IntegrationTest'                         # Integration tests (Docker required)
./mvnw clean install                                                      # Full build (Docker required)
```

## New Service from Template

1. Copy `ddd-base-project/` → `{service-name}/`
2. `pom.xml` (root + modules) → update `artifactId` and `name`
3. Update `application.yml` → rename `spring.application.name`
4. Rename package `com.zenvite2.base` → `com.zenvite2.{service}`
5. Rename `BaseApplication` → `{Service}Application`
6. Study the sample code — note file locations, class names, and how layers connect (these are reference examples for the architecture)
7. Remove sample code (grep `TODO: Remove this sample code`)
