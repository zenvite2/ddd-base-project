package vn.com.viettel.vds.ntbh.base.adapter.inbound.restful.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// TODO: Remove this sample code when implementing the actual service
public record OrderResponse(
    UUID id,
    String customerName,
    String status,
    BigDecimal totalAmount,
    List<OrderItemResponse> items) {}
