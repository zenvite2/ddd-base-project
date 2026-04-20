package vn.com.viettel.vds.ntbh.base.adapter.outbound.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.com.viettel.vds.domain.ddd.DomainEvent;
import vn.com.viettel.vds.event.DomainEventHandler;

// TODO: Remove this sample code when implementing the actual service
@Slf4j
@Component
public class LoggingEventHandler implements DomainEventHandler {

  @Override
  public void handle(DomainEvent event) {
    log.info(
        "[DOMAIN EVENT] type={}, subject={}, occurredAt={}",
        event.type().fullName(),
        event.subject(),
        event.occurredAt().value());
  }
}
