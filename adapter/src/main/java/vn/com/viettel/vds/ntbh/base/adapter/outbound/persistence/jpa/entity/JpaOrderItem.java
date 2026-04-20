package vn.com.viettel.vds.ntbh.base.adapter.outbound.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.viettel.vds.ntbh.base.domain.value.OrderItemStatus;

// TODO: Remove this sample code when implementing the actual service
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class JpaOrderItem {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private JpaOrder order;

  @Column(name = "product_name", nullable = false)
  private String productName;

  @Column(nullable = false)
  private int quantity;

  @Column(name = "unit_price", nullable = false)
  private BigDecimal unitPrice;

  @Column(nullable = false)
  private OrderItemStatus status;
}
