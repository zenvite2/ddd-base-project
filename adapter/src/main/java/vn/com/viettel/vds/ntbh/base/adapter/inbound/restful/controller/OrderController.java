package vn.com.viettel.vds.ntbh.base.adapter.inbound.restful.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.com.viettel.vds.ntbh.base.adapter.inbound.facade.OrderCommandFacade;
import vn.com.viettel.vds.ntbh.base.adapter.inbound.restful.dto.request.AddOrderItemRequest;
import vn.com.viettel.vds.ntbh.base.adapter.inbound.restful.dto.request.CreateOrderRequest;
import vn.com.viettel.vds.ntbh.base.adapter.inbound.restful.dto.request.UpdateOrderItemRequest;
import vn.com.viettel.vds.ntbh.base.adapter.inbound.restful.dto.response.OrderResponse;
import vn.com.viettel.vds.ntbh.base.adapter.inbound.restful.dto.response.PagedOrderResponse;
import vn.com.viettel.vds.web.ApiResponse;

// TODO: Remove this sample code when implementing the actual service
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderCommandFacade orderFacade;

  @PostMapping
  public ResponseEntity<ApiResponse<OrderResponse>> create(
      @Valid @RequestBody CreateOrderRequest request) {
    OrderResponse response = orderFacade.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<PagedOrderResponse>> search(
      @RequestParam(required = false) String customerName,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    PagedOrderResponse response = orderFacade.search(customerName, status, page, size);
    return ResponseEntity.ok(ApiResponse.of(response));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<OrderResponse>> findById(@PathVariable UUID id) {
    OrderResponse response = orderFacade.findById(id);
    return ResponseEntity.ok(ApiResponse.of(response));
  }

  @PostMapping("/{id}/items")
  public ResponseEntity<ApiResponse<OrderResponse>> addItem(
      @PathVariable UUID id, @Valid @RequestBody AddOrderItemRequest request) {
    OrderResponse response = orderFacade.addItem(id, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
  }

  @PutMapping("/{id}/items/{itemId}")
  public ResponseEntity<ApiResponse<OrderResponse>> updateItemQuantity(
      @PathVariable UUID id,
      @PathVariable UUID itemId,
      @Valid @RequestBody UpdateOrderItemRequest request) {
    OrderResponse response = orderFacade.updateItemQuantity(id, itemId, request);
    return ResponseEntity.ok(ApiResponse.of(response));
  }

  @DeleteMapping("/{id}/items/{itemId}")
  public ResponseEntity<ApiResponse<OrderResponse>> removeItem(
      @PathVariable UUID id, @PathVariable UUID itemId) {
    OrderResponse response = orderFacade.removeItem(id, itemId);
    return ResponseEntity.ok(ApiResponse.of(response));
  }

  @PutMapping("/{id}/confirm")
  public ResponseEntity<ApiResponse<OrderResponse>> confirm(@PathVariable UUID id) {
    OrderResponse response = orderFacade.confirm(id);
    return ResponseEntity.ok(ApiResponse.of(response));
  }

  @PutMapping("/{id}/cancel")
  public ResponseEntity<ApiResponse<OrderResponse>> cancel(@PathVariable UUID id) {
    OrderResponse response = orderFacade.cancel(id);
    return ResponseEntity.ok(ApiResponse.of(response));
  }
}
