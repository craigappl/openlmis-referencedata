package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.OrderableProduct;

import java.util.UUID;

/**
 * Persistence repository for saving/finding {@link OrderableProduct}.
 */
public interface OrderableProductRepository extends
    ReferenceDataRepository<OrderableProduct, UUID> {

  @Override
  <S extends OrderableProduct> S save(S entity);

  @Override
  <S extends OrderableProduct> Iterable<S> save(Iterable<S> entities);

  <S extends OrderableProduct> S findByProductCode(Code code);

}