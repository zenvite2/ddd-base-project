package com.zenvite2.base.application.dto;

import java.util.UUID;

// TODO: Remove this sample code when implementing the actual service
public record OrderFulfillmentResult(UUID orderId, boolean success, String failureReason) {}
