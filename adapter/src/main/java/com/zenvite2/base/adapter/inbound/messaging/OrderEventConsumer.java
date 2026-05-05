package com.zenvite2.base.adapter.inbound.messaging;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// TODO: Remove this sample code when implementing the actual service
// Consumer receives events from Kafka — call Facade for business processing, do not call UseCase
// directly
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class OrderEventConsumer {

  @KafkaListener(topicPattern = "base\\.order\\..*")
  public void handleOrderEvent(Map<String, Object> envelope) {
    log.info(
        "[KAFKA CONSUMER] Received event: eventId={}, eventType={}, aggregateId={}, aggregateType={}",
        envelope.get("eventId"),
        envelope.get("eventType"),
        envelope.get("aggregateId"),
        envelope.get("aggregateType"));
    // TODO: Call Facade to process business logic if needed
  }
}
