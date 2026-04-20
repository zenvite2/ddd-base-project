package vn.com.viettel.vds.ntbh.base.adapter.inbound.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.com.viettel.vds.ntbh.base.application.port.inbound.OrderCommandPort;

// TODO: Remove this sample code when implementing the actual service
// Facade only delegates — does NOT contain business logic
@Component
@RequiredArgsConstructor
@Transactional
public class OrderCleanupFacade {

  private final OrderCommandPort orderCommandPort;

  public int cleanupExpiredOrders(int retentionDays) {
    return orderCommandPort.cleanupExpiredOrders(retentionDays);
  }
}
