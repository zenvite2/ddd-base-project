package com.zenvite2.base.adapter.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.zenvite2.base.application.port.inbound.OrderCommandPort;
import com.zenvite2.base.application.port.inbound.OrderQueryPort;
import com.zenvite2.base.application.usecase.OrderCommandUseCase;
import com.zenvite2.base.application.usecase.OrderQueryUseCase;
import com.zenvite2.base.domain.repository.OrderRepository;

// TODO: Remove this sample config and replace with actual use cases
// UseCase is NOT a Spring bean — instantiated manually via @Bean
// BeanPostProcessor automatically wraps proxy for methods annotated with @EventPublishHandler
@Configuration
public class UseCaseConfig {

  @Bean
  public OrderCommandPort orderCommandPort(OrderRepository orderRepository) {
    return new OrderCommandUseCase(orderRepository);
  }

  @Bean
  public OrderQueryPort orderQueryPort(OrderRepository orderRepository) {
    return new OrderQueryUseCase(orderRepository);
  }
}
