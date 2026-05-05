package com.zenvite2.base.adapter.outbound.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.zenvite2.base.domain.value.OrderItemStatus;

@Converter(autoApply = true)
public class OrderItemStatusConverter implements AttributeConverter<OrderItemStatus, Integer> {

  @Override
  public Integer convertToDatabaseColumn(OrderItemStatus status) {
    return status == null ? null : status.code();
  }

  @Override
  public OrderItemStatus convertToEntityAttribute(Integer code) {
    return code == null ? null : OrderItemStatus.fromCode(code);
  }
}
