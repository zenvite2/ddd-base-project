package com.zenvite2.base.adapter.outbound.persistence.jpa.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.zenvite2.base.domain.value.OrderStatus;

// TODO: Remove this sample code when implementing the actual service
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class JpaOrder {

  @Id private UUID id;

  @Column(name = "customer_name", nullable = false)
  private String customerName;

  @Column(nullable = false)
  private OrderStatus status;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @OneToMany(
      mappedBy = "order",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  private List<JpaOrderItem> items = new ArrayList<>();
}
