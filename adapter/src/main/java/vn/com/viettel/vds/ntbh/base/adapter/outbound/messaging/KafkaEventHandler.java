package vn.com.viettel.vds.ntbh.base.adapter.outbound.messaging;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import vn.com.viettel.vds.domain.ddd.DomainEvent;
import vn.com.viettel.vds.domain.ddd.DomainEventType;
import vn.com.viettel.vds.event.DomainEventHandler;

// TODO: Remove this sample code when implementing the actual service
// Publish domain event to Kafka — envelope per architecture.md spec
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class KafkaEventHandler implements DomainEventHandler {

  /** Base fields from AbstractDomainEvent — excluded from payload extraction. */
  private static final Set<String> BASE_FIELDS =
      Set.of("eventId", "subject", "aggregateType", "type", "occurredAt", "eventVersion");

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ObjectMapper eventMapper;
  private final String serviceName;

  public KafkaEventHandler(
      KafkaTemplate<String, Object> kafkaTemplate,
      @Value("${spring.application.name}") String serviceName) {
    this.kafkaTemplate = kafkaTemplate;
    this.serviceName = serviceName;
    // Domain events use field access (no getters) → need a dedicated ObjectMapper
    this.eventMapper = new ObjectMapper();
    eventMapper.setVisibility(
        eventMapper
            .getSerializationConfig()
            .getDefaultVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
    eventMapper.registerModule(new JavaTimeModule());
    eventMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    eventMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  @Override
  public void handle(DomainEvent event) {
    String topic = toTopic(event.type());
    Map<String, Object> envelope = toEnvelope(event);

    kafkaTemplate
        .send(topic, event.subject(), envelope)
        .whenComplete(
            (result, ex) -> {
              if (ex != null) {
                log.error(
                    "[KAFKA] Failed to send event: topic={}, aggregateId={}",
                    topic,
                    event.subject(),
                    ex);
              } else {
                log.info(
                    "[KAFKA] Event sent: topic={}, aggregateId={}, offset={}",
                    topic,
                    event.subject(),
                    result.getRecordMetadata().offset());
              }
            });
  }

  private String toTopic(DomainEventType type) {
    return "ntbh." + type.boundedContext() + "." + type.name();
  }

  /** Build Kafka event envelope per architecture.md spec. */
  private Map<String, Object> toEnvelope(DomainEvent event) {
    Map<String, Object> envelope = new LinkedHashMap<>();
    envelope.put("eventId", event.eventId());
    envelope.put("eventType", event.type().fullName());
    envelope.put("eventVersion", event.eventVersion());
    envelope.put("occurredAt", event.occurredAt().value().toString());
    envelope.put("tenantId", null); // TODO: TenantContext.get() when implementing multi-tenant
    envelope.put("aggregateId", event.subject());
    envelope.put("aggregateType", event.aggregateType());
    envelope.put("payload", toPayload(event));

    Map<String, Object> metadata = new LinkedHashMap<>();
    metadata.put("correlationId", null); // TODO: CorrelationContext.get() when implementing tracing
    metadata.put("causationId", null); // TODO: set from parent event if applicable
    metadata.put("publishedBy", serviceName);
    envelope.put("metadata", metadata);

    return envelope;
  }

  /** Extract event-specific fields as payload (exclude base DomainEvent fields). */
  @SuppressWarnings("unchecked")
  private Map<String, Object> toPayload(DomainEvent event) {
    Map<String, Object> allFields = eventMapper.convertValue(event, Map.class);
    allFields.keySet().removeAll(BASE_FIELDS);
    return allFields;
  }
}
