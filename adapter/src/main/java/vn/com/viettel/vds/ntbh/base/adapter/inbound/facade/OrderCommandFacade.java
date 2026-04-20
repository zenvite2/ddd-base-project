package vn.com.viettel.vds.ntbh.base.adapter.inbound.facade;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.com.viettel.vds.ntbh.base.adapter.inbound.restful.dto.request.AddOrderItemRequest;
import vn.com.viettel.vds.ntbh.base.adapter.inbound.restful.dto.request.CreateOrderRequest;
import vn.com.viettel.vds.ntbh.base.adapter.inbound.restful.dto.request.UpdateOrderItemRequest;
import vn.com.viettel.vds.ntbh.base.adapter.inbound.restful.dto.response.OrderItemResponse;
import vn.com.viettel.vds.ntbh.base.adapter.inbound.restful.dto.response.OrderResponse;
import vn.com.viettel.vds.ntbh.base.adapter.inbound.restful.dto.response.PagedOrderResponse;
import vn.com.viettel.vds.ntbh.base.application.dto.AddOrderItemCommand;
import vn.com.viettel.vds.ntbh.base.application.dto.CreateOrderCommand;
import vn.com.viettel.vds.ntbh.base.application.dto.OrderResult;
import vn.com.viettel.vds.ntbh.base.application.dto.PagedOrderResult;
import vn.com.viettel.vds.ntbh.base.application.dto.SearchOrderQuery;
import vn.com.viettel.vds.ntbh.base.application.port.inbound.OrderCommandPort;
import vn.com.viettel.vds.ntbh.base.application.port.inbound.OrderQueryPort;

// TODO: Remove this sample code when implementing the actual service
// Facade only maps DTOs and delegates — does NOT contain business logic
@Component
@RequiredArgsConstructor
@Transactional
public class OrderCommandFacade {

  private final OrderCommandPort orderCommandPort;
  private final OrderQueryPort orderQueryPort;

  public OrderResponse create(CreateOrderRequest request) {
    OrderResult result = orderCommandPort.create(new CreateOrderCommand(request.customerName()));
    return toResponse(result);
  }

  @CacheEvict(value = "orders", key = "#orderId")
  public OrderResponse addItem(UUID orderId, AddOrderItemRequest request) {
    OrderResult result =
        orderCommandPort.addItem(
            new AddOrderItemCommand(
                orderId, request.productName(), request.quantity(), request.unitPrice()));
    return toResponse(result);
  }

  @CacheEvict(value = "orders", key = "#orderId")
  public OrderResponse updateItemQuantity(
      UUID orderId, UUID itemId, UpdateOrderItemRequest request) {
    OrderResult result = orderCommandPort.updateItemQuantity(orderId, itemId, request.quantity());
    return toResponse(result);
  }

  @CacheEvict(value = "orders", key = "#orderId")
  public OrderResponse removeItem(UUID orderId, UUID itemId) {
    OrderResult result = orderCommandPort.removeItem(orderId, itemId);
    return toResponse(result);
  }

  @CacheEvict(value = "orders", key = "#orderId")
  public OrderResponse confirm(UUID orderId) {
    OrderResult result = orderCommandPort.confirm(orderId);
    return toResponse(result);
  }

  @CacheEvict(value = "orders", key = "#orderId")
  public OrderResponse cancel(UUID orderId) {
    OrderResult result = orderCommandPort.cancel(orderId);
    return toResponse(result);
  }

  @Cacheable(value = "orders", key = "#id")
  public OrderResponse findById(UUID id) {
    OrderResult result = orderQueryPort.findById(id);
    return toResponse(result);
  }

  public PagedOrderResponse search(String customerName, String status, int page, int size) {
    PagedOrderResult result =
        orderQueryPort.search(new SearchOrderQuery(customerName, status, page, size));
    List<OrderResponse> orders = result.content().stream().map(this::toResponse).toList();
    return new PagedOrderResponse(orders, result.page(), result.size(), result.nextPage());
  }

  private OrderResponse toResponse(OrderResult result) {
    List<OrderItemResponse> items =
        result.items().stream()
            .map(
                i ->
                    new OrderItemResponse(
                        i.id(),
                        i.productName(),
                        i.quantity(),
                        i.unitPrice(),
                        i.lineTotal(),
                        i.status()))
            .toList();
    return new OrderResponse(
        result.id(), result.customerName(), result.status(), result.totalAmount(), items);
  }
}
