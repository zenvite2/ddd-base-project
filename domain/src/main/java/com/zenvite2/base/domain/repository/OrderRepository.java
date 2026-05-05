package com.zenvite2.base.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import vn.com.viettel.vds.domain.ddd.Repository;
import com.zenvite2.base.domain.aggregate.Order;
import com.zenvite2.base.domain.value.OrderId;

// TODO: Remove this sample code when implementing the actual service
//
// ⚠️ CONVENTION: Prefer findBy(Criteria) for queries that can be expressed via Criteria.
// Only add custom methods when:
//   - Query is too complex for Criteria (multi-join, aggregation, native SQL)
//   - Performance requires a dedicated optimized query
//   - Semantics are clearer (e.g.: permanentlyRemoveAll for hard delete)
public interface OrderRepository extends Repository<Order> {

  /** Find orders that were soft-deleted before the given cutoff time */
  List<Order> findSoftDeletedBefore(LocalDateTime cutoff);

  /** Permanently remove orders — used for cleanup after retention period expires */
  void permanentlyRemoveAll(List<OrderId> ids);
}
