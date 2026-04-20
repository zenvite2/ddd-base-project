package vn.com.viettel.vds.ntbh.base.application.usecase;

import java.util.List;
import java.util.UUID;
import vn.com.viettel.vds.domain.ddd.PagedSearchResult;
import vn.com.viettel.vds.domain.ddd.Pagination;
import vn.com.viettel.vds.domain.exception.ResourceNotFoundException;
import vn.com.viettel.vds.ntbh.base.application.dto.OrderItemResult;
import vn.com.viettel.vds.ntbh.base.application.dto.OrderResult;
import vn.com.viettel.vds.ntbh.base.application.dto.PagedOrderResult;
import vn.com.viettel.vds.ntbh.base.application.dto.SearchOrderQuery;
import vn.com.viettel.vds.ntbh.base.application.port.inbound.OrderQueryPort;
import vn.com.viettel.vds.ntbh.base.domain.aggregate.Order;
import vn.com.viettel.vds.ntbh.base.domain.criteria.OrderSearchCriteria;
import vn.com.viettel.vds.ntbh.base.domain.repository.OrderRepository;
import vn.com.viettel.vds.ntbh.base.domain.value.OrderId;
import vn.com.viettel.vds.ntbh.base.domain.value.OrderStatus;

// TODO: Remove this sample code when implementing the actual service
public class OrderQueryUseCase implements OrderQueryPort {

  private final OrderRepository orderRepository;

  public OrderQueryUseCase(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Override
  public OrderResult findById(UUID id) {
    Order order =
        orderRepository
            .findById(OrderId.of(id))
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    return toResult(order);
  }

  @Override
  public PagedOrderResult search(SearchOrderQuery query) {
    OrderStatus status = query.status() != null ? OrderStatus.valueOf(query.status()) : null;
    OrderSearchCriteria criteria = OrderSearchCriteria.of(query.customerName(), status);
    Pagination pagination = Pagination.of(query.page(), query.size());

    PagedSearchResult<Order> result = orderRepository.findBy(criteria, pagination);

    List<OrderResult> orders = result.content().stream().map(this::toResult).toList();
    return new PagedOrderResult(orders, result.page(), result.size(), result.nextPage());
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
