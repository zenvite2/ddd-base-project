package vn.com.viettel.vds.ntbh.base.domain.aggregate;

import java.math.BigDecimal;
import java.util.Objects;
import vn.com.viettel.vds.AssertionConcern;
import vn.com.viettel.vds.domain.ddd.Entity;
import vn.com.viettel.vds.domain.ddd.UuidValueGenerator;
import vn.com.viettel.vds.ntbh.base.domain.value.OrderItemId;
import vn.com.viettel.vds.ntbh.base.domain.value.OrderItemStatus;

// TODO: Remove this sample code when implementing the actual service
// Entity belonging to the Order aggregate — has its own identity, mutable state, always loaded with
// Order.
// All operations on OrderItem must go through the Order aggregate root.
public class OrderItem implements Entity {

  // No instance needed — use UuidValueGenerator.generate() directly

  private final OrderItemId id;
  private final String productName;
  private int quantity;
  private final BigDecimal unitPrice;
  private OrderItemStatus status;

  private OrderItem(
      OrderItemId id,
      String productName,
      int quantity,
      BigDecimal unitPrice,
      OrderItemStatus status) {
    this.id = id;
    this.productName = productName;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
    this.status = status;
  }

  /** Factory method — entity creates its own identity */
  public static OrderItem create(String productName, int quantity, BigDecimal unitPrice) {
    AssertionConcern.assertArgumentNotEmpty(productName, "Product name must not be blank");
    AssertionConcern.assertArgumentTrue(quantity > 0, "Quantity must be greater than 0");
    AssertionConcern.assertArgumentNotNull(unitPrice, "Unit price must not be null");
    AssertionConcern.assertArgumentTrue(
        unitPrice.compareTo(BigDecimal.ZERO) > 0, "Unit price must be greater than 0");
    OrderItemId id = OrderItemId.of(UuidValueGenerator.generate());
    return new OrderItem(id, productName, quantity, unitPrice, OrderItemStatus.PENDING);
  }

  /** Reconstitution from persistence */
  public static OrderItem reconstitute(
      OrderItemId id,
      String productName,
      int quantity,
      BigDecimal unitPrice,
      OrderItemStatus status) {
    return new OrderItem(id, productName, quantity, unitPrice, status);
  }

  public OrderItemId id() {
    return id;
  }

  public String productName() {
    return productName;
  }

  public int quantity() {
    return quantity;
  }

  public BigDecimal unitPrice() {
    return unitPrice;
  }

  public OrderItemStatus status() {
    return status;
  }

  public BigDecimal lineTotal() {
    return unitPrice.multiply(BigDecimal.valueOf(quantity));
  }

  void changeQuantity(int newQuantity) {
    AssertionConcern.assertArgumentTrue(newQuantity > 0, "Quantity must be greater than 0");
    this.quantity = newQuantity;
  }

  void markShipped() {
    this.status = OrderItemStatus.SHIPPED;
  }

  void markReturned() {
    this.status = OrderItemStatus.RETURNED;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OrderItem that = (OrderItem) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
