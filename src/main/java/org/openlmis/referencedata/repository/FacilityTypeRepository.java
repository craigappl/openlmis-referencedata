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
import org.openlmis.referencedata.domain.FacilityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.UUID;

@JaversSpringDataAuditable
public interface FacilityTypeRepository extends JpaRepository<FacilityType, UUID> {

  @Override
  <S extends FacilityType> S save(S entity);

  FacilityType findOneByCode(@Param("code") String code);

  Page<FacilityType> findByIdIn(Collection<UUID> id, Pageable pageable);

  Page<FacilityType> findByIdInAndActive(Collection<UUID> id, Boolean active, Pageable pageable);

  Page<FacilityType> findByActive(Boolean active, Pageable pageable);

  boolean existsByCode(@Param("code") String code);
}
