package vn.com.viettel.vds.ntbh.base.domain.value;

// TODO: Remove this sample code when implementing the actual service
public enum OrderItemStatus {
  PENDING(0),
  SHIPPED(1),
  RETURNED(2);

  private final int code;

  OrderItemStatus(int code) {
    this.code = code;
  }

  public int code() {
    return code;
  }

  public static OrderItemStatus fromCode(int code) {
    for (OrderItemStatus s : values()) {
      if (s.code == code) return s;
    }
    throw new IllegalArgumentException("Unknown OrderItemStatus code: " + code);
  }
}
