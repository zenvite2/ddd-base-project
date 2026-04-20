package vn.com.viettel.vds.ntbh.base.adapter.inbound.facade;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import vn.com.viettel.vds.ntbh.base.adapter.configuration.TemporalConfig;
import vn.com.viettel.vds.ntbh.base.adapter.inbound.restful.dto.request.StartFulfillmentRequest;
import vn.com.viettel.vds.ntbh.base.adapter.inbound.restful.dto.response.WorkflowStartResponse;
import vn.com.viettel.vds.ntbh.base.adapter.inbound.restful.dto.response.WorkflowStatusResponse;
import vn.com.viettel.vds.ntbh.base.adapter.inbound.temporal.TemporalOrderFulfillmentWorkflow;
import vn.com.viettel.vds.ntbh.base.application.dto.OrderFulfillmentCommand;

// TODO: Remove this sample code when implementing the actual service
// Facade maps request DTOs, starts workflow, returns workflowId — no business logic
@Component
@RequiredArgsConstructor
@ConditionalOnBean(WorkflowClient.class)
public class OrderWorkflowFacade {

  private final WorkflowClient workflowClient;

  public WorkflowStartResponse startFulfillment(StartFulfillmentRequest request) {
    String workflowId = "order-fulfillment-" + request.orderId();

    WorkflowOptions options =
        WorkflowOptions.newBuilder()
            .setWorkflowId(workflowId)
            .setTaskQueue(TemporalConfig.ORDER_FULFILLMENT_QUEUE)
            .build();

    TemporalOrderFulfillmentWorkflow workflow =
        workflowClient.newWorkflowStub(TemporalOrderFulfillmentWorkflow.class, options);

    OrderFulfillmentCommand cmd =
        new OrderFulfillmentCommand(
            request.orderId(), request.customerName(), request.totalAmount());

    WorkflowClient.start(workflow::execute, cmd);

    return new WorkflowStartResponse(workflowId, "PROCESSING");
  }

  public WorkflowStatusResponse getStatus(String workflowId) {
    TemporalOrderFulfillmentWorkflow workflow =
        workflowClient.newWorkflowStub(TemporalOrderFulfillmentWorkflow.class, workflowId);
    return new WorkflowStatusResponse(workflowId, workflow.getStatus().name());
  }
}
