package vn.com.viettel.vds.ntbh.base.application.dto;

import java.util.UUID;

// TODO: Remove this sample code when implementing the actual service
public record OrderFulfillmentResult(UUID orderId, boolean success, String failureReason) {}
