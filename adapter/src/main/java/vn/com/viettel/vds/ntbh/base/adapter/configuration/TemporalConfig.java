package vn.com.viettel.vds.ntbh.base.adapter.configuration;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.com.viettel.vds.ntbh.base.adapter.inbound.temporal.OrderFulfillmentActivitiesImpl;
import vn.com.viettel.vds.ntbh.base.adapter.inbound.temporal.OrderFulfillmentWorkflowImpl;

// TODO: Remove this sample config when implementing the actual service
// Temporal worker registration — connects to Temporal cluster and registers workflow + activities
@Configuration
@ConditionalOnProperty(name = "temporal.service-address")
public class TemporalConfig {

  public static final String ORDER_FULFILLMENT_QUEUE = "order-fulfillment";

  @Bean
  public WorkflowServiceStubs workflowServiceStubs(
      @Value("${temporal.service-address}") String serviceAddress) {
    return WorkflowServiceStubs.newServiceStubs(
        WorkflowServiceStubsOptions.newBuilder().setTarget(serviceAddress).build());
  }

  @Bean
  public WorkflowClient workflowClient(
      WorkflowServiceStubs stubs, @Value("${temporal.namespace:default}") String namespace) {
    return WorkflowClient.newInstance(
        stubs, WorkflowClientOptions.newBuilder().setNamespace(namespace).build());
  }

  @Bean(destroyMethod = "shutdown")
  public WorkerFactory workerFactory(
      WorkflowClient workflowClient, OrderFulfillmentActivitiesImpl activitiesImpl) {
    WorkerFactory factory = WorkerFactory.newInstance(workflowClient);
    Worker worker = factory.newWorker(ORDER_FULFILLMENT_QUEUE);

    worker.registerWorkflowImplementationTypes(OrderFulfillmentWorkflowImpl.class);
    worker.registerActivitiesImplementations(activitiesImpl);

    factory.start();
    return factory;
  }
}
