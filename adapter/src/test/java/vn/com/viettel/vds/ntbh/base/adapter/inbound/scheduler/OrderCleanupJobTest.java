package vn.com.viettel.vds.ntbh.base.adapter.inbound.scheduler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.viettel.vds.ntbh.base.adapter.inbound.facade.OrderCleanupFacade;

// TODO: Remove this sample code when implementing the actual service
@ExtendWith(MockitoExtension.class)
class OrderCleanupJobTest {

  @Mock private OrderCleanupFacade orderCleanupFacade;

  private OrderCleanupJob job;

  @BeforeEach
  void setUp() {
    job = new OrderCleanupJob(orderCleanupFacade);
  }

  @Test
  void should_delegate_to_facade_with_retention_days() {
    when(orderCleanupFacade.cleanupExpiredOrders(OrderCleanupJob.RETENTION_DAYS)).thenReturn(5);

    job.cleanupDeletedOrders();

    verify(orderCleanupFacade).cleanupExpiredOrders(OrderCleanupJob.RETENTION_DAYS);
  }

  @Test
  void should_complete_when_no_expired_orders() {
    when(orderCleanupFacade.cleanupExpiredOrders(OrderCleanupJob.RETENTION_DAYS)).thenReturn(0);

    job.cleanupDeletedOrders();

    verify(orderCleanupFacade).cleanupExpiredOrders(OrderCleanupJob.RETENTION_DAYS);
  }
}
