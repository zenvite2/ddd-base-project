package com.zenvite2.base.domain.value;

// TODO: Remove this sample code when implementing the actual service
public enum OrderStatus {
  DRAFT(0),
  CONFIRMED(1),
  SHIPPED(2),
  DELIVERED(3),
  CANCELLED(4);

  private final int code;

  OrderStatus(int code) {
    this.code = code;
  }

  public int code() {
    return code;
  }

  public static OrderStatus fromCode(int code) {
    for (OrderStatus s : values()) {
      if (s.code == code) return s;
    }
    throw new IllegalArgumentException("Unknown OrderStatus code: " + code);
  }
}
