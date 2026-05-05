package com.zenvite2.base.adapter.outbound.persistence.repository;

import org.springframework.data.jpa.domain.Specification;
import com.zenvite2.base.adapter.outbound.persistence.jpa.entity.JpaOrder;
import com.zenvite2.base.domain.criteria.OrderSearchCriteria;

// TODO: Remove this sample code when implementing the actual service
public final class OrderSpecification {

  private OrderSpecification() {}

  public static Specification<JpaOrder> fromCriteria(OrderSearchCriteria criteria) {
    Specification<JpaOrder> spec =
        Specification.where((root, query, cb) -> cb.isNull(root.get("deletedAt")));

    if (criteria.hasCustomerName()) {
      String pattern = "%" + criteria.customerName().toLowerCase() + "%";
      spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("customerName")), pattern));
    }
    if (criteria.hasStatus()) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), criteria.status()));
    }

    return spec;
  }
}
