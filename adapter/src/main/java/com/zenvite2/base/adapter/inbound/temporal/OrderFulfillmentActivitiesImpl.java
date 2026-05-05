package com.zenvite2.base.adapter.inbound.temporal;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.zenvite2.base.application.port.inbound.OrderCommandPort;
import com.zenvite2.base.application.dto.PaymentRequestResult;

// TODO: Remove this sample code when implementing the actual service
// Activity implementation — delegates to UseCases (local) or HTTP clients (remote services)
// Each activity method is independently retryable and has its own timeout
@Component
@RequiredArgsConstructor
public class OrderFulfillmentActivitiesImpl implements TemporalOrderFulfillmentActivities {

  private final OrderCommandPort orderCommandPort;
  // TODO: Inject HTTP clients for remote service calls
  // private final PaymentServiceClient paymentClient;
  // private final ShippingServiceClient shippingClient;

  @Override
  public void confirmOrder(String orderId) {
    // Local domain logic — delegates to UseCase
    orderCommandPort.confirm(UUID.fromString(orderId));
  }

  @Override
  public PaymentRequestResult requestPayment(String orderId, long amount) {
    // TODO: Replace with actual HTTP call to payment-service
    // return paymentClient.requestPayment(orderId, amount);
    return new PaymentRequestResult("txn-" + orderId, "PENDING");
  }

  @Override
  public void shipOrder(String orderId) {
    // TODO: Replace with actual HTTP call to shipping/logistics service
    // shippingClient.createShipment(orderId);
  }

  @Override
  public void completeOrder(String orderId) {
    // TODO: Replace with actual domain logic to mark order as delivered
    // orderCommandPort.complete(UUID.fromString(orderId));
  }
}
