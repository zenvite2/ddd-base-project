package vn.com.viettel.vds.ntbh.base.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.viettel.vds.domain.exception.BusinessRuleViolationException;
import vn.com.viettel.vds.ntbh.base.application.dto.AddOrderItemCommand;
import vn.com.viettel.vds.ntbh.base.application.dto.CreateOrderCommand;
import vn.com.viettel.vds.ntbh.base.application.dto.OrderResult;
import vn.com.viettel.vds.ntbh.base.application.usecase.OrderCommandUseCase;
import vn.com.viettel.vds.ntbh.base.domain.aggregate.Order;
import vn.com.viettel.vds.ntbh.base.domain.aggregate.OrderItem;
import vn.com.viettel.vds.ntbh.base.domain.repository.OrderRepository;
import vn.com.viettel.vds.ntbh.base.domain.value.OrderId;

// TODO: Remove this sample code when implementing the actual service
@ExtendWith(MockitoExtension.class)
class OrderCommandUseCaseTest {

  @Mock private OrderRepository orderRepository;

  private OrderCommandUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new OrderCommandUseCase(orderRepository);
  }

  @Test
  void should_create_order_with_draft_status() {
    CreateOrderCommand command = new CreateOrderCommand("Nguyen Van A");

    OrderResult result = useCase.create(command);

    assertThat(result.customerName()).isEqualTo("Nguyen Van A");
    assertThat(result.status()).isEqualTo("DRAFT");
    assertThat(result.items()).isEmpty();
    verify(orderRepository).save(any(Order.class));
  }

  @Test
  void should_add_item_to_order() {
    Order order = Order.create("Test");
    when(orderRepository.findById(any(OrderId.class))).thenReturn(Optional.of(order));

    AddOrderItemCommand command =
        new AddOrderItemCommand(order.id().value(), "Product A", 2, new BigDecimal("50000"));
    OrderResult result = useCase.addItem(command);

    assertThat(result.items()).hasSize(1);
    assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("100000"));
    verify(orderRepository).save(any(Order.class));
  }

  @Test
  void should_confirm_order_with_items() {
    Order order = Order.create("Test");
    order.addItem(OrderItem.create("A", 1, new BigDecimal("10000")));
    when(orderRepository.findById(any(OrderId.class))).thenReturn(Optional.of(order));

    OrderResult result = useCase.confirm(order.id().value());

    assertThat(result.status()).isEqualTo("CONFIRMED");
  }

  @Test
  void should_throw_when_confirm_empty_order() {
    Order order = Order.create("Test");
    when(orderRepository.findById(any(OrderId.class))).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> useCase.confirm(order.id().value()))
        .isInstanceOf(BusinessRuleViolationException.class);
  }

  @Test
  void should_cancel_order() {
    Order order = Order.create("Test");
    when(orderRepository.findById(any(OrderId.class))).thenReturn(Optional.of(order));

    OrderResult result = useCase.cancel(order.id().value());

    assertThat(result.status()).isEqualTo("CANCELLED");
  }

  // === Cleanup expired orders ===

  @Test
  void should_cleanup_expired_orders() {
    Order order1 = Order.create("Expired 1");
    Order order2 = Order.create("Expired 2");
    when(orderRepository.findSoftDeletedBefore(any(LocalDateTime.class)))
        .thenReturn(List.of(order1, order2));

    int count = useCase.cleanupExpiredOrders(30);

    assertThat(count).isEqualTo(2);
    verify(orderRepository).permanentlyRemoveAll(List.of(order1.id(), order2.id()));
  }

  @Test
  void should_return_zero_when_no_expired_orders() {
    when(orderRepository.findSoftDeletedBefore(any(LocalDateTime.class)))
        .thenReturn(Collections.emptyList());

    int count = useCase.cleanupExpiredOrders(30);

    assertThat(count).isZero();
    verify(orderRepository, never()).permanentlyRemoveAll(anyList());
  }
}
