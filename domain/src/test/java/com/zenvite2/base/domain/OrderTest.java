package com.zenvite2.base.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import vn.com.viettel.vds.domain.ddd.DomainEvent;
import vn.com.viettel.vds.domain.ddd.DomainEventPublisher;
import vn.com.viettel.vds.domain.exception.BusinessRuleViolationException;
import vn.com.viettel.vds.domain.exception.InvalidArgumentException;
import vn.com.viettel.vds.domain.exception.ResourceNotFoundException;
import com.zenvite2.base.domain.aggregate.Order;
import com.zenvite2.base.domain.aggregate.OrderItem;
import com.zenvite2.base.domain.event.OrderCancelledEvent;
import com.zenvite2.base.domain.event.OrderConfirmedEvent;
import com.zenvite2.base.domain.value.OrderItemId;
import com.zenvite2.base.domain.value.OrderStatus;

// TODO: Remove this sample code when implementing the actual service
class OrderTest {

  @AfterEach
  void clearEvents() {
    DomainEventPublisher.clear();
  }

  // === Aggregate root ===

  @Test
  void should_create_order_with_draft_status() {
    Order order = createOrder();

    assertThat(order.status()).isEqualTo(OrderStatus.DRAFT);
    assertThat(order.items()).isEmpty();
    assertThat(order.totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void should_throw_when_customer_name_empty() {
    assertThatThrownBy(() -> Order.create("")).isInstanceOf(InvalidArgumentException.class);
  }

  // === OrderItem entity ===

  @Test
  void should_add_item_when_draft() {
    Order order = createOrder();

    order.addItem(createItem("Product A", 2, "50000"));

    assertThat(order.items()).hasSize(1);
    assertThat(order.totalAmount()).isEqualByComparingTo(new BigDecimal("100000"));
  }

  @Test
  void should_calculate_total_from_multiple_items() {
    Order order = createOrder();
    order.addItem(createItem("A", 2, "50000"));
    order.addItem(createItem("B", 1, "30000"));

    assertThat(order.totalAmount()).isEqualByComparingTo(new BigDecimal("130000"));
  }

  @Test
  void should_update_item_quantity_when_draft() {
    Order order = createOrder();
    OrderItem item = createItem("A", 2, "50000");
    order.addItem(item);

    order.updateItemQuantity(item.id(), 5);

    assertThat(order.items().get(0).quantity()).isEqualTo(5);
    assertThat(order.totalAmount()).isEqualByComparingTo(new BigDecimal("250000"));
  }

  @Test
  void should_remove_item_when_draft() {
    Order order = createOrder();
    OrderItem item = createItem("A", 1, "10000");
    order.addItem(item);

    order.removeItem(item.id());

    assertThat(order.items()).isEmpty();
  }

  @Test
  void should_throw_when_item_not_found() {
    Order order = createOrder();
    OrderItemId unknownId = OrderItemId.of(UUID.randomUUID());

    assertThatThrownBy(() -> order.updateItemQuantity(unknownId, 1))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void should_throw_when_exceed_max_items() {
    Order order = createOrder();
    for (int i = 0; i < 20; i++) {
      order.addItem(createItem("Item " + i, 1, "1000"));
    }

    assertThatThrownBy(() -> order.addItem(createItem("Extra", 1, "1000")))
        .isInstanceOf(BusinessRuleViolationException.class);
  }

  // === Order status ===

  @Test
  void should_confirm_order_with_items() {
    Order order = createOrder();
    order.addItem(createItem("A", 1, "10000"));

    order.confirm();

    assertThat(order.status()).isEqualTo(OrderStatus.CONFIRMED);
  }

  @Test
  void should_throw_when_confirm_empty_order() {
    Order order = createOrder();

    assertThatThrownBy(order::confirm).isInstanceOf(BusinessRuleViolationException.class);
  }

  @Test
  void should_throw_when_add_item_to_confirmed_order() {
    Order order = createOrder();
    order.addItem(createItem("A", 1, "10000"));
    order.confirm();

    assertThatThrownBy(() -> order.addItem(createItem("B", 1, "5000")))
        .isInstanceOf(BusinessRuleViolationException.class);
  }

  @Test
  void should_cancel_draft_order() {
    Order order = createOrder();

    order.cancel();

    assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
  }

  @Test
  void should_cancel_confirmed_order() {
    Order order = createOrder();
    order.addItem(createItem("A", 1, "10000"));
    order.confirm();

    order.cancel();

    assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
  }

  @Test
  void should_throw_when_cancel_already_cancelled() {
    Order order = createOrder();
    order.cancel();

    assertThatThrownBy(order::cancel).isInstanceOf(BusinessRuleViolationException.class);
  }

  // === Domain Event envelope fields ===

  @Test
  void should_publish_confirmed_event_with_envelope_fields() {
    Order order = createOrder();
    order.addItem(createItem("A", 2, "50000"));

    order.confirm();

    List<DomainEvent> events = DomainEventPublisher.getEvents();
    assertThat(events).hasSize(1);

    DomainEvent event = events.get(0);
    assertThat(event).isInstanceOf(OrderConfirmedEvent.class);
    assertThat(event.eventId()).isNotBlank();
    assertThat(event.type().fullName()).isEqualTo("order.confirmed");
    assertThat(event.eventVersion()).isEqualTo("1.0");
    assertThat(event.subject()).isEqualTo(order.id().toString());
    assertThat(event.aggregateType()).isEqualTo("Order");
    assertThat(event.occurredAt()).isNotNull();

    OrderConfirmedEvent confirmed = (OrderConfirmedEvent) event;
    assertThat(confirmed.customerName()).isEqualTo("Test Customer");
    assertThat(confirmed.totalAmount()).isEqualByComparingTo(new BigDecimal("100000"));
    assertThat(confirmed.itemCount()).isEqualTo(1);
  }

  @Test
  void should_publish_cancelled_event_with_envelope_fields() {
    Order order = createOrder();

    order.cancel();

    List<DomainEvent> events = DomainEventPublisher.getEvents();
    assertThat(events).hasSize(1);

    DomainEvent event = events.get(0);
    assertThat(event).isInstanceOf(OrderCancelledEvent.class);
    assertThat(event.eventId()).isNotBlank();
    assertThat(event.type().fullName()).isEqualTo("order.cancelled");
    assertThat(event.eventVersion()).isEqualTo("1.0");
    assertThat(event.subject()).isEqualTo(order.id().toString());
    assertThat(event.aggregateType()).isEqualTo("Order");
    assertThat(event.occurredAt()).isNotNull();
  }

  // === Helpers ===

  private Order createOrder() {
    return Order.create("Test Customer");
  }

  private OrderItem createItem(String name, int quantity, String price) {
    return OrderItem.create(name, quantity, new BigDecimal(price));
  }
}
