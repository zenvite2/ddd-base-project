package vn.com.viettel.vds.ntbh.base.application.dto;

import java.util.List;
import java.util.Optional;

// TODO: Remove this sample code when implementing the actual service
public record PagedOrderResult(
    List<OrderResult> content, int page, int size, Optional<Integer> nextPage) {}
