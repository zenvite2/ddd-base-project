package vn.com.viettel.vds.ntbh.base.adapter.inbound.messaging;

import io.temporal.client.WorkflowClient;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import vn.com.viettel.vds.ntbh.base.adapter.inbound.temporal.TemporalOrderFulfillmentWorkflow;
import vn.com.viettel.vds.ntbh.base.application.dto.PaymentCallbackResult;

// TODO: Remove this sample code when implementing the actual service
// Kafka consumer receives async payment result and signals the waiting workflow
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(WorkflowClient.class)
public class PaymentResultConsumer {

  private final WorkflowClient workflowClient;

  @KafkaListener(topics = "ntbh.payment.result", groupId = "${spring.application.name}")
  public void onPaymentResult(Map<String, Object> event) {
    String orderId = (String) event.get("orderId");
    String transactionId = (String) event.get("transactionId");
    boolean success = Boolean.TRUE.equals(event.get("success"));
    String reason = (String) event.get("reason");

    String workflowId = "order-fulfillment-" + orderId;

    log.info("Received payment result for workflow {}: success={}", workflowId, success);

    TemporalOrderFulfillmentWorkflow workflow =
        workflowClient.newWorkflowStub(TemporalOrderFulfillmentWorkflow.class, workflowId);

    workflow.onPaymentResult(new PaymentCallbackResult(transactionId, success, reason));
  }
}
