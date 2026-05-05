package com.zenvite2.base.adapter.inbound.restful.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

// TODO: Remove this sample code when implementing the actual service
public record StartFulfillmentRequest(
    @NotNull UUID orderId, @NotBlank String customerName, @Positive long totalAmount) {}
