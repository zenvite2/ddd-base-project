package com.zenvite2.base.adapter.inbound.temporal;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import com.zenvite2.base.application.dto.OrderFulfillmentCommand;
import com.zenvite2.base.application.dto.OrderFulfillmentResult;
import com.zenvite2.base.application.dto.OrderFulfillmentStatus;
import com.zenvite2.base.application.dto.PaymentCallbackResult;

// TODO: Remove this sample code when implementing the actual service
// Temporal-annotated interface wrapping the plain Java application interface
// Temporal SDK dependency lives ONLY in adapter layer
@WorkflowInterface
public interface TemporalOrderFulfillmentWorkflow {

  @WorkflowMethod
  OrderFulfillmentResult execute(OrderFulfillmentCommand cmd);

  @SignalMethod
  void onPaymentResult(PaymentCallbackResult result);

  @QueryMethod
  OrderFulfillmentStatus getStatus();
}
