package com.zenvite2.base.adapter.outbound.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import vn.com.viettel.vds.domain.ddd.Criteria;
import vn.com.viettel.vds.domain.ddd.Identity;
import vn.com.viettel.vds.domain.ddd.PagedSearchResult;
import vn.com.viettel.vds.domain.ddd.Pagination;
import vn.com.viettel.vds.domain.exception.ResourceNotFoundException;
import com.zenvite2.base.adapter.outbound.persistence.jpa.entity.JpaOrder;
import com.zenvite2.base.adapter.outbound.persistence.jpa.repository.JpaOrderRepository;
import com.zenvite2.base.adapter.outbound.persistence.mapper.OrderMapper;
import com.zenvite2.base.domain.aggregate.Order;
import com.zenvite2.base.domain.criteria.OrderSearchCriteria;
import com.zenvite2.base.domain.repository.OrderRepository;
import com.zenvite2.base.domain.value.OrderId;

// TODO: Remove this sample code when implementing the actual service
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

  private final JpaOrderRepository jpaRepository;
  private final OrderMapper mapper;

  @Override
  public void save(Order aggregate) {
    JpaOrder entity =
        jpaRepository
            .findById(aggregate.id().value())
            .map(
                existing -> {
                  existing.setCustomerName(aggregate.customerName());
                  existing.setStatus(aggregate.status());
                  existing.getItems().clear();
                  existing.getItems().addAll(mapper.toJpaItems(aggregate, existing));
                  return existing;
                })
            .orElseGet(() -> mapper.toJpa(aggregate));
    jpaRepository.save(entity);
  }

  @Override
  public <U extends Identity<?>> Optional<Order> findById(U id) {
    UUID uuid = ((OrderId) id).value();
    return jpaRepository.findById(uuid).map(mapper::toDomain);
  }

  @Override
  public <U extends Identity<?>> List<Order> findAllByIds(List<U> ids) {
    List<UUID> uuids = ids.stream().map(id -> ((OrderId) id).value()).toList();
    return jpaRepository.findAllById(uuids).stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<Order> findBy(Criteria criteria) {
    OrderSearchCriteria orderCriteria = (OrderSearchCriteria) criteria;
    Specification<JpaOrder> spec = OrderSpecification.fromCriteria(orderCriteria);
    return jpaRepository.findAll(spec).stream().map(mapper::toDomain).toList();
  }

  @Override
  public PagedSearchResult<Order> findBy(Criteria criteria, Pagination pagination) {
    OrderSearchCriteria orderCriteria = (OrderSearchCriteria) criteria;
    Specification<JpaOrder> spec = OrderSpecification.fromCriteria(orderCriteria);
    PageRequest pageRequest =
        PageRequest.of(
            pagination.page(), pagination.size(), Sort.by(Sort.Direction.DESC, "createdAt"));

    Page<JpaOrder> page = jpaRepository.findAll(spec, pageRequest);

    List<Order> content = page.getContent().stream().map(mapper::toDomain).toList();
    Optional<Integer> nextPage =
        page.hasNext() ? Optional.of(pagination.page() + 1) : Optional.empty();

    return new PagedSearchResult<>(content, pagination.page(), pagination.size(), nextPage);
  }

  @Override
  public List<Order> findSoftDeletedBefore(LocalDateTime cutoff) {
    return jpaRepository.findByDeletedAtNotNullAndDeletedAtBefore(cutoff).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public void permanentlyRemoveAll(List<com.zenvite2.base.domain.value.OrderId> ids) {
    List<UUID> uuids =
        ids.stream().map(com.zenvite2.base.domain.value.OrderId::value).toList();
    List<JpaOrder> orders = jpaRepository.findAllById(uuids);
    jpaRepository.deleteAll(orders);
  }

  @Override
  public <U extends Identity<?>> void delete(U id) {
    UUID uuid = ((OrderId) id).value();
    JpaOrder entity =
        jpaRepository
            .findById(uuid)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + uuid));
    entity.setDeletedAt(LocalDateTime.now());
    jpaRepository.save(entity);
  }

  @Override
  public <U extends Identity<?>> void deleteAllByIds(List<U> ids) {
    List<UUID> uuids = ids.stream().map(id -> ((OrderId) id).value()).toList();
    LocalDateTime now = LocalDateTime.now();
    List<JpaOrder> entities = jpaRepository.findAllById(uuids);
    entities.forEach(entity -> entity.setDeletedAt(now));
    jpaRepository.saveAll(entities);
  }
}
