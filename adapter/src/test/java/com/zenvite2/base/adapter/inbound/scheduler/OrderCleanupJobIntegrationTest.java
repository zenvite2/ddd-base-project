package com.zenvite2.base.adapter.inbound.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.zenvite2.base.adapter.outbound.persistence.jpa.entity.JpaOrder;
import com.zenvite2.base.adapter.outbound.persistence.jpa.entity.JpaOrderItem;
import com.zenvite2.base.adapter.outbound.persistence.jpa.repository.JpaOrderRepository;
import com.zenvite2.base.domain.value.OrderItemStatus;
import com.zenvite2.base.domain.value.OrderStatus;

// TODO: Remove this sample code when implementing the actual service
@SpringBootTest
@ActiveProfiles("test")
class OrderCleanupJobIntegrationTest {

  @Autowired private OrderCleanupJob job;
  @Autowired private JpaOrderRepository jpaOrderRepository;

  @BeforeEach
  void setUp() {
    jpaOrderRepository.deleteAll();
  }

  @Test
  void should_permanently_delete_orders_soft_deleted_over_30_days_ago() {
    // Order soft-deleted 31 days ago — must be permanently deleted
    JpaOrder expiredOrder =
        createOrder("Old Customer", OrderStatus.CANCELLED, LocalDateTime.now().minusDays(31));

    // Order soft-deleted 10 days ago — not yet expired, keep
    JpaOrder recentOrder =
        createOrder(
            "Recently Deleted Customer", OrderStatus.CANCELLED, LocalDateTime.now().minusDays(10));

    // Order not yet soft-deleted — keep
    JpaOrder activeOrder = createOrder("Active Customer", OrderStatus.DRAFT, null);

    job.cleanupDeletedOrders();

    assertThat(jpaOrderRepository.findById(expiredOrder.getId())).isEmpty();
    assertThat(jpaOrderRepository.findById(recentOrder.getId())).isPresent();
    assertThat(jpaOrderRepository.findById(activeOrder.getId())).isPresent();
  }

  @Test
  void should_delete_order_items_via_cascade() {
    // Order with item, soft-deleted 31 days ago — both order and item are deleted
    JpaOrder order =
        createOrderWithItem(
            "Cascade Test Customer", OrderStatus.CANCELLED, LocalDateTime.now().minusDays(31));

    job.cleanupDeletedOrders();

    assertThat(jpaOrderRepository.findById(order.getId())).isEmpty();
  }

  @Test
  void should_do_nothing_when_no_expired_orders() {
    createOrder("Not Deleted Customer", OrderStatus.DRAFT, null);
    createOrder(
        "Recently Deleted Customer", OrderStatus.CANCELLED, LocalDateTime.now().minusDays(5));

    job.cleanupDeletedOrders();

    assertThat(jpaOrderRepository.count()).isEqualTo(2);
  }

  @Test
  void should_delete_orders_exactly_at_30_day_boundary() {
    // Order soft-deleted exactly 30 days and 1 second ago — must be deleted
    JpaOrder borderlineExpired =
        createOrder(
            "Boundary Customer",
            OrderStatus.CANCELLED,
            LocalDateTime.now().minusDays(30).minusSeconds(1));

    // Order soft-deleted exactly 29 days ago — keep
    JpaOrder borderlineRecent =
        createOrder("Recent Customer", OrderStatus.CANCELLED, LocalDateTime.now().minusDays(29));

    job.cleanupDeletedOrders();

    assertThat(jpaOrderRepository.findById(borderlineExpired.getId())).isEmpty();
    assertThat(jpaOrderRepository.findById(borderlineRecent.getId())).isPresent();
  }

  @Test
  void should_delete_regardless_of_order_status() {
    // All statuses are deleted if deleted_at > 30 days
    JpaOrder cancelledOrder =
        createOrder("Cancelled", OrderStatus.CANCELLED, LocalDateTime.now().minusDays(31));
    JpaOrder confirmedOrder =
        createOrder("Confirmed", OrderStatus.CONFIRMED, LocalDateTime.now().minusDays(31));
    JpaOrder draftOrder =
        createOrder("Draft", OrderStatus.DRAFT, LocalDateTime.now().minusDays(31));

    job.cleanupDeletedOrders();

    assertThat(jpaOrderRepository.findById(cancelledOrder.getId())).isEmpty();
    assertThat(jpaOrderRepository.findById(confirmedOrder.getId())).isEmpty();
    assertThat(jpaOrderRepository.findById(draftOrder.getId())).isEmpty();
    assertThat(jpaOrderRepository.count()).isZero();
  }

  // === Helpers ===

  private JpaOrder createOrder(String customerName, OrderStatus status, LocalDateTime deletedAt) {
    JpaOrder order = new JpaOrder();
    order.setId(UUID.randomUUID());
    order.setCustomerName(customerName);
    order.setStatus(status);
    order.setCreatedAt(LocalDateTime.now().minusDays(60));
    order.setDeletedAt(deletedAt);
    return jpaOrderRepository.save(order);
  }

  private JpaOrder createOrderWithItem(
      String customerName, OrderStatus status, LocalDateTime deletedAt) {
    JpaOrder order = new JpaOrder();
    order.setId(UUID.randomUUID());
    order.setCustomerName(customerName);
    order.setStatus(status);
    order.setCreatedAt(LocalDateTime.now().minusDays(60));
    order.setDeletedAt(deletedAt);

    JpaOrderItem item = new JpaOrderItem();
    item.setId(UUID.randomUUID());
    item.setOrder(order);
    item.setProductName("Test Product");
    item.setQuantity(1);
    item.setUnitPrice(new BigDecimal("10000"));
    item.setStatus(OrderItemStatus.PENDING);
    order.getItems().add(item);

    return jpaOrderRepository.save(order);
  }
}
