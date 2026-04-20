package vn.com.viettel.vds.ntbh.base.adapter.inbound.restful.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

// TODO: Remove this sample code when implementing the actual service
public record AddOrderItemRequest(
    @NotBlank(message = "Product name must not be blank") String productName,
    @Min(value = 1, message = "Quantity must be >= 1") int quantity,
    @NotNull(message = "Unit price must not be null") @Positive(message = "Unit price must be > 0")
        BigDecimal unitPrice) {}
