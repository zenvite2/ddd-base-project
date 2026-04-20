package vn.com.viettel.vds.ntbh.base.adapter.inbound.temporal;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import vn.com.viettel.vds.ntbh.base.application.dto.PaymentRequestResult;

// TODO: Remove this sample code when implementing the actual service
// Temporal-annotated activity interface — wraps plain Java application interface
@ActivityInterface
public interface TemporalOrderFulfillmentActivities {

  @ActivityMethod
  void confirmOrder(String orderId);

  @ActivityMethod
  PaymentRequestResult requestPayment(String orderId, long amount);

  @ActivityMethod
  void shipOrder(String orderId);

  @ActivityMethod
  void completeOrder(String orderId);
}
