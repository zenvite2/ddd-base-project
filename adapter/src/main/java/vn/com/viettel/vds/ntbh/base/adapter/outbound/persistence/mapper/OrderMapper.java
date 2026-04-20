package vn.com.viettel.vds.ntbh.base.adapter.outbound.persistence.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import vn.com.viettel.vds.ntbh.base.adapter.outbound.persistence.jpa.entity.JpaOrder;
import vn.com.viettel.vds.ntbh.base.adapter.outbound.persistence.jpa.entity.JpaOrderItem;
import vn.com.viettel.vds.ntbh.base.domain.aggregate.Order;
import vn.com.viettel.vds.ntbh.base.domain.aggregate.OrderItem;
import vn.com.viettel.vds.ntbh.base.domain.value.OrderId;
import vn.com.viettel.vds.ntbh.base.domain.value.OrderItemId;

// TODO: Remove this sample code when implementing the actual service
@Component
public class OrderMapper {

  public JpaOrder toJpa(Order order) {
    JpaOrder jpa = new JpaOrder();
    jpa.setId(order.id().value());
    jpa.setCustomerName(order.customerName());
    jpa.setStatus(order.status());
    jpa.setCreatedAt(order.createdAt());

    List<JpaOrderItem> jpaItems = order.items().stream().map(item -> toJpaItem(item, jpa)).toList();
    jpa.setItems(jpaItems);

    return jpa;
  }

  public List<JpaOrderItem> toJpaItems(Order order, JpaOrder jpaOrder) {
    return order.items().stream().map(item -> toJpaItem(item, jpaOrder)).toList();
  }

  public Order toDomain(JpaOrder jpa) {
    List<OrderItem> items = jpa.getItems().stream().map(this::toDomainItem).toList();

    return Order.reconstitute(
        OrderId.of(jpa.getId()), jpa.getCustomerName(), jpa.getStatus(), items, jpa.getCreatedAt());
  }

  private JpaOrderItem toJpaItem(OrderItem item, JpaOrder jpaOrder) {
    JpaOrderItem jpa = new JpaOrderItem();
    jpa.setId(item.id().value());
    jpa.setOrder(jpaOrder);
    jpa.setProductName(item.productName());
    jpa.setQuantity(item.quantity());
    jpa.setUnitPrice(item.unitPrice());
    jpa.setStatus(item.status());
    return jpa;
  }

  private OrderItem toDomainItem(JpaOrderItem jpa) {
    return OrderItem.reconstitute(
        OrderItemId.of(jpa.getId()),
        jpa.getProductName(),
        jpa.getQuantity(),
        jpa.getUnitPrice(),
        jpa.getStatus());
  }
}
