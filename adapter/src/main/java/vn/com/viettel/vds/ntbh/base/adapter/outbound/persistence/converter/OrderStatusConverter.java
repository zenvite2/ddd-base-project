package vn.com.viettel.vds.ntbh.base.adapter.outbound.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import vn.com.viettel.vds.ntbh.base.domain.value.OrderStatus;

@Converter(autoApply = true)
public class OrderStatusConverter implements AttributeConverter<OrderStatus, Integer> {

  @Override
  public Integer convertToDatabaseColumn(OrderStatus status) {
    return status == null ? null : status.code();
  }

  @Override
  public OrderStatus convertToEntityAttribute(Integer code) {
    return code == null ? null : OrderStatus.fromCode(code);
  }
}
