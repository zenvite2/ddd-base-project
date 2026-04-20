package vn.com.viettel.vds.ntbh.base.adapter.inbound.restful.dto.response;

import java.util.List;
import java.util.Optional;

// TODO: Remove this sample code when implementing the actual service
public record PagedOrderResponse(
    List<OrderResponse> content, int page, int size, Optional<Integer> nextPage) {}
