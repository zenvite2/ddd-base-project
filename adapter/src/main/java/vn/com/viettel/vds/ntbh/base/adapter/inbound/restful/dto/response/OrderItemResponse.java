package vn.com.viettel.vds.ntbh.base.adapter.inbound.restful.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

// TODO: Remove this sample code when implementing the actual service
public record OrderItemResponse(
    UUID id,
    String productName,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal lineTotal,
    String status) {}
