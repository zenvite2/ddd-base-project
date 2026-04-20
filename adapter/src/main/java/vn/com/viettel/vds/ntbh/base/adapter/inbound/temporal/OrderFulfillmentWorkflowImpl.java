package vn.com.viettel.vds.ntbh.base.adapter.inbound.temporal;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import vn.com.viettel.vds.ntbh.base.application.dto.OrderFulfillmentCommand;
import vn.com.viettel.vds.ntbh.base.application.dto.OrderFulfillmentResult;
import vn.com.viettel.vds.ntbh.base.application.dto.OrderFulfillmentStatus;
import vn.com.viettel.vds.ntbh.base.application.dto.PaymentCallbackResult;

// TODO: Remove this sample code when implementing the actual service
// Workflow implementation — orchestration logic only, no business logic
// Activities handle actual work (domain calls, HTTP calls to other services)
public class OrderFulfillmentWorkflowImpl implements TemporalOrderFulfillmentWorkflow {

  private final TemporalOrderFulfillmentActivities activities =
      Workflow.newActivityStub(
          TemporalOrderFulfillmentActivities.class,
          ActivityOptions.newBuilder()
              .setStartToCloseTimeout(Duration.ofSeconds(10))
              .setRetryOptions(
                  RetryOptions.newBuilder()
                      .setMaximumAttempts(3)
                      .setDoNotRetry(IllegalArgumentException.class.getName())
                      .build())
              .build());

  private OrderFulfillmentStatus status = OrderFulfillmentStatus.STARTED;
  private PaymentCallbackResult paymentResult = null;

  @Override
  public OrderFulfillmentResult execute(OrderFulfillmentCommand cmd) {
    String orderId = cmd.orderId().toString();

    // Step 1: Confirm order (local domain logic via UseCase)
    status = OrderFulfillmentStatus.CONFIRMING;
    activities.confirmOrder(orderId);

    // Step 2: Request payment (HTTP call to payment-service or external gateway)
    status = OrderFulfillmentStatus.AWAITING_PAYMENT;
    activities.requestPayment(orderId, cmd.totalAmount());

    // Wait for async payment callback via signal (max 30 minutes)
    boolean received = Workflow.await(Duration.ofMinutes(30), () -> paymentResult != null);

    if (!received) {
      status = OrderFulfillmentStatus.PAYMENT_FAILED;
      throw ApplicationFailure.newFailure("Payment timeout after 30 minutes", "PAYMENT_TIMEOUT");
    }
    if (!paymentResult.success()) {
      status = OrderFulfillmentStatus.PAYMENT_FAILED;
      return new OrderFulfillmentResult(cmd.orderId(), false, paymentResult.reason());
    }

    // Step 3: Ship order (HTTP call to logistics/shipping service)
    status = OrderFulfillmentStatus.SHIPPING;
    activities.shipOrder(orderId);

    // Step 4: Complete order (update local domain state)
    status = OrderFulfillmentStatus.COMPLETED;
    activities.completeOrder(orderId);

    return new OrderFulfillmentResult(cmd.orderId(), true, null);
  }

  @Override
  public void onPaymentResult(PaymentCallbackResult result) {
    this.paymentResult = result;
  }

  @Override
  public OrderFulfillmentStatus getStatus() {
    return status;
  }
}
