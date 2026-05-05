package com.zenvite2.base.domain.event;

import vn.com.viettel.vds.domain.ddd.AbstractDomainEvent;
import vn.com.viettel.vds.domain.ddd.DomainEventType;
import vn.com.viettel.vds.domain.type.DTime;

// TODO: Remove this sample code when implementing the actual service
public class OrderCancelledEvent extends AbstractDomainEvent {

  public OrderCancelledEvent(String orderId) {
    super(DomainEventType.of("order", "cancelled"), "Order", DTime.now());
    this.subject = orderId;
  }
}
