package vn.com.viettel.vds.ntbh.base.application.port.inbound;

import java.util.UUID;
import vn.com.viettel.vds.ntbh.base.application.dto.OrderResult;
import vn.com.viettel.vds.ntbh.base.application.dto.PagedOrderResult;
import vn.com.viettel.vds.ntbh.base.application.dto.SearchOrderQuery;

// TODO: Remove this sample code when implementing the actual service
public interface OrderQueryPort {

  OrderResult findById(UUID id);

  PagedOrderResult search(SearchOrderQuery query);
}
