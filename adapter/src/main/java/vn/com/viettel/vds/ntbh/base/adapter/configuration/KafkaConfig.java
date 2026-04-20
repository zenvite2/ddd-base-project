package vn.com.viettel.vds.ntbh.base.adapter.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

// TODO: Remove this sample config when implementing the actual service
// Topic naming convention: ntbh.{domain}.{event}
@Configuration
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class KafkaConfig {

  @Bean
  public NewTopic orderConfirmedTopic() {
    return TopicBuilder.name("ntbh.order.confirmed").partitions(3).replicas(1).build();
  }

  @Bean
  public NewTopic orderCancelledTopic() {
    return TopicBuilder.name("ntbh.order.cancelled").partitions(3).replicas(1).build();
  }
}
