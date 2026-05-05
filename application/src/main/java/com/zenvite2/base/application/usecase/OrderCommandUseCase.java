package com.zenvite2.base.application.usecase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import vn.com.viettel.vds.domain.exception.ResourceNotFoundException;
import vn.com.viettel.vds.event.EventPublishHandler;
import com.zenvite2.base.application.dto.AddOrderItemCommand;
import com.zenvite2.base.application.dto.CreateOrderCommand;
import com.zenvite2.base.application.dto.OrderItemResult;
import com.zenvite2.base.application.dto.OrderResult;
import com.zenvite2.base.application.port.inbound.OrderCommandPort;
import com.zenvite2.base.domain.aggregate.Order;
import com.zenvite2.base.domain.aggregate.OrderItem;
import com.zenvite2.base.domain.repository.OrderRepository;
import com.zenvite2.base.domain.value.OrderId;
import com.zenvite2.base.domain.value.OrderItemId;

// TODO: Remove this sample code when implementing the actual service
// UseCase is NOT a Spring bean — instantiated via @Configuration @Bean in adapter
public class OrderCommandUseCase implements OrderCommandPort {

  private final OrderRepository orderRepository;

  public OrderCommandUseCase(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Override
  public OrderResult create(CreateOrderCommand command) {
    Order order = Order.create(command.customerName());
    orderRepository.save(order);
    return toResult(order);
  }

  @Override
  public OrderResult addItem(AddOrderItemCommand command) {
    Order order = findOrder(command.orderId());
    OrderItem item =
        OrderItem.create(command.productName(), command.quantity(), command.unitPrice());
    order.addItem(item);
    orderRepository.save(order);
    return toResult(order);
  }

  @Override
  public OrderResult updateItemQuantity(UUID orderId, UUID itemId, int quantity) {
    Order order = findOrder(orderId);
    order.updateItemQuantity(OrderItemId.of(itemId), quantity);
    orderRepository.save(order);
    return toResult(order);
  }

  @Override
  public OrderResult removeItem(UUID orderId, UUID itemId) {
    Order order = findOrder(orderId);
    order.removeItem(OrderItemId.of(itemId));
    orderRepository.save(order);
    return toResult(order);
  }

  @Override
  @EventPublishHandler
  public OrderResult confirm(UUID orderId) {
    Order order = findOrder(orderId);
    order.confirm();
    orderRepository.save(order);
    return toResult(order);
  }

  @Override
  public OrderResult cancel(UUID orderId) {
    Order order = findOrder(orderId);
    order.cancel();
    orderRepository.save(order);
    return toResult(order);
  }

  @Override
  public int cleanupExpiredOrders(int retentionDays) {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
    List<Order> expiredOrders = orderRepository.findSoftDeletedBefore(cutoff);
    if (expiredOrders.isEmpty()) {
      return 0;
    }
    List<OrderId> ids = expiredOrders.stream().map(Order::id).toList();
    orderRepository.permanentlyRemoveAll(ids);
    return ids.size();
  }

  private Order findOrder(UUID orderId) {
    return orderRepository
        .findById(OrderId.of(orderId))
        .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
  }

  private OrderResult toResult(Order order) {
    List<OrderItemResult> items =
        order.items().stream()
            .map(
                i ->
                    new OrderItemResult(
                        i.id().value(),
                        i.productName(),
                        i.quantity(),
                        i.unitPrice(),
                        i.lineTotal(),
                        i.status().name()))
            .toList();
    return new OrderResult(
        order.id().value(),
        order.customerName(),
        order.status().name(),
        order.totalAmount(),
        items);
  }
}
