# Kafka Event Envelope Standardization

## Goal
Align domain event + Kafka message format with architecture.md spec.

## Current State
`KafkaEventHandler.toMessage()` produces only 3 fields:
```json
{"eventType": "order/confirmed", "subject": "uuid", "occurredAt": "..."}
```

## Target State (per architecture.md line 514-544)
```json
{
  "eventId": "uuid-v7",
  "eventType": "order.confirmed",
  "eventVersion": "1.0",
  "occurredAt": "2026-03-26T10:00:00+07:00",
  "tenantId": "insurer-cbh",
  "aggregateId": "order-uuid",
  "aggregateType": "Order",
  "payload": { "customerName": "...", "totalAmount": 150000 },
  "metadata": {
    "correlationId": "request-uuid",
    "causationId": "parent-event-uuid",
    "publishedBy": "ntbh-base-service"
  }
}
```

## Phases

### Phase 1: domain-core (vds-platform) — [pending]
Add missing fields to `DomainEvent` interface + `AbstractDomainEvent`.

**Files:**
- `vds-platform/domain-core/.../ddd/DomainEvent.java` — add `eventId()`, `eventVersion()`, `aggregateType()`
- `vds-platform/domain-core/.../ddd/AbstractDomainEvent.java` — generate eventId (UUID v7), default eventVersion "1.0", accept aggregateType
- `vds-platform/domain-core/.../ddd/DomainEventType.java` — change delimiter from `/` to `.` in `fullName()`

### Phase 2: ntbh-base-project — [pending]
Update adapter to build full envelope, update sample events.

**Files:**
- `domain/.../event/OrderConfirmedEvent.java` — pass aggregateType "Order"
- `domain/.../event/OrderCancelledEvent.java` — pass aggregateType "Order"
- `adapter/.../messaging/KafkaEventHandler.java` — build full envelope with all fields
- `adapter/.../messaging/OrderEventConsumer.java` — parse new envelope format

### Deferred (future services)
- `TenantContext` (ThreadLocal) — not yet implemented, envelope will use `null` for tenantId
- `CorrelationContext` — not yet implemented, envelope will use generated UUID for correlationId
- These will be added when implementing actual services (identity-service, etc.)

## Risk
- DomainEventType delimiter change (`/` → `.`) — breaking change for existing consumers
  - Mitigation: only sample code exists, no production consumers
- eventId generation adds uuid-creator dependency to domain-core
  - Already present via UuidValueGenerator

## Success Criteria
- [ ] `DomainEvent` has eventId, eventVersion, aggregateType
- [ ] `KafkaEventHandler` produces full envelope matching architecture.md spec
- [ ] `OrderEventConsumer` parses new envelope
- [ ] All tests pass
- [ ] `./gradlew build -x test` compiles OK for both vds-platform and ntbh-base-project
