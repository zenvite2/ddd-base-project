package vn.com.viettel.vds.ntbh.base.domain.criteria;

import vn.com.viettel.vds.domain.ddd.Criteria;
import vn.com.viettel.vds.ntbh.base.domain.value.OrderStatus;

// TODO: Remove this sample code when implementing the actual service
public class OrderSearchCriteria implements Criteria {

  private final String customerName;
  private final OrderStatus status;

  private OrderSearchCriteria(String customerName, OrderStatus status) {
    this.customerName = customerName;
    this.status = status;
  }

  public static OrderSearchCriteria of(String customerName, OrderStatus status) {
    return new OrderSearchCriteria(customerName, status);
  }

  public String customerName() {
    return customerName;
  }

  public OrderStatus status() {
    return status;
  }

  public boolean hasCustomerName() {
    return customerName != null && !customerName.isBlank();
  }

  public boolean hasStatus() {
    return status != null;
  }
}
