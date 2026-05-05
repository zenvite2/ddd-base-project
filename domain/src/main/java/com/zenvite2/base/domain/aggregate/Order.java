package com.zenvite2.base.domain.aggregate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import vn.com.viettel.vds.AssertionConcern;
import vn.com.viettel.vds.domain.ddd.Aggregate;
import vn.com.viettel.vds.domain.ddd.DomainEventPublisher;
import vn.com.viettel.vds.domain.ddd.UuidValueGenerator;
import vn.com.viettel.vds.domain.exception.BusinessRuleViolationException;
import vn.com.viettel.vds.domain.exception.ResourceNotFoundException;
import com.zenvite2.base.domain.event.OrderCancelledEvent;
import com.zenvite2.base.domain.event.OrderConfirmedEvent;
import com.zenvite2.base.domain.value.OrderId;
import com.zenvite2.base.domain.value.OrderItemId;
import com.zenvite2.base.domain.value.OrderStatus;

// TODO: Remove this sample code when implementing the actual service
public class Order extends Aggregate<OrderId> {

  private static final int MAX_ITEMS = 20;

  private String customerName;
  private OrderStatus status;
  private List<OrderItem> items;
  private LocalDateTime createdAt;

  protected Order() {}

  /** Factory method — aggregate creates its own identity */
  public static Order create(String customerName) {
    AssertionConcern.assertArgumentNotEmpty(customerName, "Customer name must not be blank");
    Order order = new Order();
    order.id = OrderId.of(UuidValueGenerator.generate());
    order.customerName = customerName;
    order.status = OrderStatus.DRAFT;
    order.items = new ArrayList<>();
    order.createdAt = LocalDateTime.now();
    return order;
  }

  /** Reconstitution from persistence — no validation, no ID generation */
  public static Order reconstitute(
      OrderId id,
      String customerName,
      OrderStatus status,
      List<OrderItem> items,
      LocalDateTime createdAt) {
    Order order = new Order();
    order.id = id;
    order.customerName = customerName;
    order.status = status;
    order.items = new ArrayList<>(items);
    order.createdAt = createdAt;
    return order;
  }

  public String customerName() {
    return customerName;
  }

  public OrderStatus status() {
    return status;
  }

  public List<OrderItem> items() {
    return Collections.unmodifiableList(items);
  }

  public LocalDateTime createdAt() {
    return createdAt;
  }

  public BigDecimal totalAmount() {
    return items.stream().map(OrderItem::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  // === OrderItem management — all operations must go through the aggregate root ===

  public void addItem(OrderItem item) {
    ensureDraft("add item");
    AssertionConcern.assertArgumentNotNull(item, "OrderItem must not be null");
    if (items.size() >= MAX_ITEMS) {
      throw new BusinessRuleViolationException(
          "Order must not have more than " + MAX_ITEMS + " items");
    }
    items.add(item);
  }

  public void updateItemQuantity(OrderItemId itemId, int newQuantity) {
    ensureDraft("update quantity");
    OrderItem item = findItem(itemId);
    item.changeQuantity(newQuantity);
  }

  public void removeItem(OrderItemId itemId) {
    ensureDraft("remove item");
    OrderItem item = findItem(itemId);
    items.remove(item);
  }

  // === Order status transitions ===

  public void confirm() {
    ensureDraft("confirm");
    if (items.isEmpty()) {
      throw new BusinessRuleViolationException("Order must have at least 1 item to confirm");
    }
    this.status = OrderStatus.CONFIRMED;
    DomainEventPublisher.publish(
        new OrderConfirmedEvent(id.toString(), customerName, totalAmount(), items.size()));
  }

  public void cancel() {
    if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
      throw new BusinessRuleViolationException(
          "Cannot cancel an order that has been delivered or is being shipped");
    }
    if (status == OrderStatus.CANCELLED) {
      throw new BusinessRuleViolationException("Order has already been cancelled");
    }
    this.status = OrderStatus.CANCELLED;
    DomainEventPublisher.publish(new OrderCancelledEvent(id.toString()));
  }

  public void returnItem(OrderItemId itemId) {
    if (status != OrderStatus.DELIVERED) {
      throw new BusinessRuleViolationException(
          "Items can only be returned when the order has been delivered");
    }
    OrderItem item = findItem(itemId);
    item.markReturned();
  }

  private void ensureDraft(String action) {
    if (status != OrderStatus.DRAFT) {
      throw new BusinessRuleViolationException(
          "Cannot " + action + " when order is in status " + status);
    }
  }

  private OrderItem findItem(OrderItemId itemId) {
    return items.stream()
        .filter(i -> i.id().equals(itemId))
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
  }
}
