package com.zenvite2.base.application.port.inbound;

import java.util.UUID;
import com.zenvite2.base.application.dto.AddOrderItemCommand;
import com.zenvite2.base.application.dto.CreateOrderCommand;
import com.zenvite2.base.application.dto.OrderResult;

// TODO: Remove this sample code when implementing the actual service
public interface OrderCommandPort {

  OrderResult create(CreateOrderCommand command);

  OrderResult addItem(AddOrderItemCommand command);

  OrderResult updateItemQuantity(UUID orderId, UUID itemId, int quantity);

  OrderResult removeItem(UUID orderId, UUID itemId);

  OrderResult confirm(UUID orderId);

  OrderResult cancel(UUID orderId);

  /**
   * Permanently remove orders soft-deleted more than retentionDays days ago. Returns count deleted.
   */
  int cleanupExpiredOrders(int retentionDays);
}
