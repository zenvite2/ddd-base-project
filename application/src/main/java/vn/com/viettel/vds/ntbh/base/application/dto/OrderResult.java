package vn.com.viettel.vds.ntbh.base.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// TODO: Remove this sample code when implementing the actual service
public record OrderResult(
    UUID id,
    String customerName,
    String status,
    BigDecimal totalAmount,
    List<OrderItemResult> items) {}
