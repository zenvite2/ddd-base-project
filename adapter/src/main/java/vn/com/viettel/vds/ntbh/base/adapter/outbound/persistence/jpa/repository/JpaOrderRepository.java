package vn.com.viettel.vds.ntbh.base.adapter.outbound.persistence.jpa.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.com.viettel.vds.ntbh.base.adapter.outbound.persistence.jpa.entity.JpaOrder;

// TODO: Remove this sample code when implementing the actual service
public interface JpaOrderRepository
    extends JpaRepository<JpaOrder, UUID>, JpaSpecificationExecutor<JpaOrder> {

  /** Find orders that were soft-deleted before the given cutoff time */
  List<JpaOrder> findByDeletedAtNotNullAndDeletedAtBefore(LocalDateTime cutoff);
}
