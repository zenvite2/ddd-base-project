package com.zenvite2.base.domain.event;

import java.math.BigDecimal;
import vn.com.viettel.vds.domain.ddd.AbstractDomainEvent;
import vn.com.viettel.vds.domain.ddd.DomainEventType;
import vn.com.viettel.vds.domain.type.DTime;

// TODO: Remove this sample code when implementing the actual service
public class OrderConfirmedEvent extends AbstractDomainEvent {

  private final String customerName;
  private final BigDecimal totalAmount;
  private final int itemCount;

  public OrderConfirmedEvent(
      String orderId, String customerName, BigDecimal totalAmount, int itemCount) {
    super(DomainEventType.of("order", "confirmed"), "Order", DTime.now());
    this.subject = orderId;
    this.customerName = customerName;
    this.totalAmount = totalAmount;
    this.itemCount = itemCount;
  }

  public String customerName() {
    return customerName;
  }

  public BigDecimal totalAmount() {
    return totalAmount;
  }

  public int itemCount() {
    return itemCount;
  }
}
