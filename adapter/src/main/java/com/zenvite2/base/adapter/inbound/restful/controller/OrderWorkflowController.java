package com.zenvite2.base.adapter.inbound.restful.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zenvite2.base.adapter.inbound.facade.OrderWorkflowFacade;
import com.zenvite2.base.adapter.inbound.restful.dto.request.StartFulfillmentRequest;
import com.zenvite2.base.adapter.inbound.restful.dto.response.WorkflowStartResponse;
import com.zenvite2.base.adapter.inbound.restful.dto.response.WorkflowStatusResponse;
import vn.com.viettel.vds.web.ApiResponse;

// TODO: Remove this sample code when implementing the actual service
// Start workflow via REST API — returns workflowId immediately for async tracking
@RestController
@RequestMapping("/api/v1/orders/workflows")
@RequiredArgsConstructor
@ConditionalOnBean(OrderWorkflowFacade.class)
public class OrderWorkflowController {

  private final OrderWorkflowFacade workflowFacade;

  @PostMapping("/fulfillment")
  public ResponseEntity<ApiResponse<WorkflowStartResponse>> startFulfillment(
      @Valid @RequestBody StartFulfillmentRequest request) {
    WorkflowStartResponse response = workflowFacade.startFulfillment(request);
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.of(response));
  }

  @GetMapping("/{workflowId}/status")
  public ResponseEntity<ApiResponse<WorkflowStatusResponse>> getStatus(
      @PathVariable String workflowId) {
    WorkflowStatusResponse status = workflowFacade.getStatus(workflowId);
    return ResponseEntity.ok(ApiResponse.of(status));
  }
}
