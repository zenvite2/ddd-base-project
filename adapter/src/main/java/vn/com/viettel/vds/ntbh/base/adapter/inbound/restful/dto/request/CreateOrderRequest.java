package vn.com.viettel.vds.ntbh.base.adapter.inbound.restful.dto.request;

import jakarta.validation.constraints.NotBlank;

// TODO: Remove this sample code when implementing the actual service
public record CreateOrderRequest(
    @NotBlank(message = "Customer name must not be blank") String customerName) {}
