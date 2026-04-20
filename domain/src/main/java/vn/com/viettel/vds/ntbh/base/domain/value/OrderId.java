package vn.com.viettel.vds.ntbh.base.domain.value;

import java.util.Objects;
import java.util.UUID;
import vn.com.viettel.vds.domain.ddd.Identity;

// TODO: Remove this sample code when implementing the actual service
public class OrderId extends Identity<UUID> {

  private final UUID id;

  public OrderId(UUID id) {
    Objects.requireNonNull(id, "OrderId must not be null");
    this.id = id;
  }

  public static OrderId of(UUID id) {
    return new OrderId(id);
  }

  @Override
  public UUID value() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OrderId that = (OrderId) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return id.toString();
  }
}
