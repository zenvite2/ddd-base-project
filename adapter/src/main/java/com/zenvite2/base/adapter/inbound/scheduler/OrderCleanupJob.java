package com.zenvite2.base.adapter.inbound.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.zenvite2.base.adapter.inbound.facade.OrderCleanupFacade;

// TODO: Remove this sample code when implementing the actual service
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCleanupJob {

  static final int RETENTION_DAYS = 30;

  private final OrderCleanupFacade orderCleanupFacade;

  /** Permanently remove orders soft-deleted more than {@value RETENTION_DAYS} days ago */
  @Scheduled(cron = "0 0 2 * * *") // Runs at 2:00 AM every day
  public void cleanupDeletedOrders() {
    log.info("Starting soft-deleted order cleanup...");
    int count = orderCleanupFacade.cleanupExpiredOrders(RETENTION_DAYS);
    if (count > 0) {
      log.info("Permanently deleted {} orders with deleted_at > {} days.", count, RETENTION_DAYS);
    } else {
      log.info("No orders require cleanup.");
    }
    log.info("Order cleanup completed.");
  }
}
