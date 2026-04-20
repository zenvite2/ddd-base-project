package vn.com.viettel.vds.ntbh.base.adapter.inbound.restful.dto.request;

import jakarta.validation.constraints.Min;

// TODO: Remove this sample code when implementing the actual service
public record UpdateOrderItemRequest(
    @Min(value = 1, message = "Quantity must be >= 1") int quantity) {}
