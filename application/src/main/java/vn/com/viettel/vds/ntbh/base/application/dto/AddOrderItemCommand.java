package vn.com.viettel.vds.ntbh.base.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

// TODO: Remove this sample code when implementing the actual service
public record AddOrderItemCommand(
    UUID orderId, String productName, int quantity, BigDecimal unitPrice) {}
