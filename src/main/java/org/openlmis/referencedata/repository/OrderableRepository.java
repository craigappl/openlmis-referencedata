/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.referencedata.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.repository.custom.OrderableRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Persistence repository for saving/finding {@link Orderable}.
 */
@JaversSpringDataAuditable
public interface OrderableRepository extends
    PagingAndSortingRepository<Orderable, UUID>, OrderableRepositoryCustom {

  @Override
  <S extends Orderable> S save(S entity);

  @Override
  <S extends Orderable> Iterable<S> save(Iterable<S> entities);

  <S extends Orderable> S findByProductCode(Code code);

  boolean existsByProductCode(Code code);

  @Query("SELECT o FROM Orderable o WHERE o.id in ?1")
  Page<Orderable> findAllByIds(Iterable<UUID> ids, Pageable pageable);

  @Query("SELECT o FROM Orderable o WHERE o.identifiers[:key] = :value")
  List<Orderable> findAllByIdentifier(@Param("key") String key, @Param("value") String value);
}
