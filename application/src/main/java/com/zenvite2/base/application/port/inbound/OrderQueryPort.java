package com.zenvite2.base.application.port.inbound;

import java.util.UUID;
import com.zenvite2.base.application.dto.OrderResult;
import com.zenvite2.base.application.dto.PagedOrderResult;
import com.zenvite2.base.application.dto.SearchOrderQuery;

// TODO: Remove this sample code when implementing the actual service
public interface OrderQueryPort {

  OrderResult findById(UUID id);

  PagedOrderResult search(SearchOrderQuery query);
}
