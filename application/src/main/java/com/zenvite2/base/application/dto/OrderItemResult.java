package com.zenvite2.base.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

// TODO: Remove this sample code when implementing the actual service
public record OrderItemResult(
    UUID id,
    String productName,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal lineTotal,
    String status) {}
