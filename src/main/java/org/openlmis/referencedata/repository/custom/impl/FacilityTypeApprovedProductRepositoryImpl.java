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

package org.openlmis.referencedata.repository.custom.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.repository.custom.FacilityTypeApprovedProductRepositoryCustom;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class FacilityTypeApprovedProductRepositoryImpl
    implements FacilityTypeApprovedProductRepositoryCustom {

  private static final String COUNT_SELECT = "SELECT COUNT(ftap.id)";
  private static final String SEARCH_SELECT = "SELECT ftap";
  private static final String SEARCH_PRODUCTS_SQL = " FROM FacilityTypeApprovedProduct ftap"
      + " INNER JOIN ftap.orderable o"
      + " INNER JOIN ftap.program p"
      + " INNER JOIN ftap.facilityType ft"
      + " INNER JOIN o.programOrderables po"
      + " INNER JOIN po.program pop"
      + " INNER JOIN po.orderableDisplayCategory"
      + " LEFT OUTER JOIN o.identifiers"
      + " WHERE ft.id = :facilityTypeId"
      + " AND po.active = TRUE"
      + " AND pop.id = p.id";

  private static final String WITH_PROGRAM = " AND p.id = :programId";
  private static final String WITH_FULL_SUPPLY = " AND po.fullSupply = :fullSupply";
  private static final String WITH_ORDERABLE_IDS = " AND o.id in :orderableIds";

  private static final String PROGRAM = "program";
  private static final String FACILITY_TYPE = "facilityType";
  private static final String ORDERED_DISPLAY_VALUE = "orderedDisplayValue";
  private static final String CODE = "code";

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Page<FacilityTypeApprovedProduct> searchProducts(UUID facilityTypeId,
                                                          UUID programId,
                                                          Boolean fullSupply,
                                                          List<UUID> orderableIds,
                                                          Pageable pageable) {
    TypedQuery<FacilityTypeApprovedProduct> query = (TypedQuery<FacilityTypeApprovedProduct>)
        createQuery(false, facilityTypeId, programId, orderableIds, fullSupply, pageable);
    TypedQuery<Long> countQuery = (TypedQuery<Long>) createQuery(true, facilityTypeId, programId,
        orderableIds, fullSupply, pageable);

    return new PageImpl<>(query.getResultList(), pageable, countQuery.getSingleResult());
  }

  @Override
  public Page<FacilityTypeApprovedProduct> searchProducts(String facilityTypeCode,
                                                          String programCode,
                                                          Pageable pageable) {
    checkNotNull(facilityTypeCode);

    CriteriaBuilder builder = entityManager.getCriteriaBuilder();

    CriteriaQuery<FacilityTypeApprovedProduct> ftapQuery =
        builder.createQuery(FacilityTypeApprovedProduct.class);
    ftapQuery = prepareQuery(facilityTypeCode, programCode, ftapQuery, false);

    CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
    countQuery = prepareQuery(facilityTypeCode, programCode, countQuery, true);

    Long count = entityManager.createQuery(countQuery).getSingleResult();

    Pair<Integer, Integer> maxAndFirst = PageableUtil.querysMaxAndFirstResult(pageable);
    List<FacilityTypeApprovedProduct> resultList = entityManager.createQuery(ftapQuery)
        .setMaxResults(maxAndFirst.getLeft())
        .setFirstResult(maxAndFirst.getRight())
        .getResultList();

    return Pagination.getPage(resultList, pageable, count);

  }

  private TypedQuery createQuery(boolean count, UUID facilityTypeId, UUID programId,
                                 List<UUID> orderableIds, Boolean fullSupply, Pageable pageable) {
    TypedQuery query;
    StringBuilder queryString = new StringBuilder(SEARCH_PRODUCTS_SQL);
    if (null != programId) {
      queryString.append(WITH_PROGRAM);
    }
    if (null != fullSupply) {
      queryString.append(WITH_FULL_SUPPLY);
    }
    if (!isEmpty(orderableIds)) {
      queryString.append(WITH_ORDERABLE_IDS);
    }

    if (count) {
      query = entityManager.createQuery(COUNT_SELECT + queryString.toString(), Long.class);

    } else {
      query = entityManager.createQuery(SEARCH_SELECT + queryString.toString(),
          FacilityTypeApprovedProduct.class);

      Pair<Integer, Integer> maxAndFirst = PageableUtil.querysMaxAndFirstResult(pageable);
      query.setMaxResults(maxAndFirst.getLeft());
      query.setFirstResult(maxAndFirst.getRight());
    }

    query.setParameter("facilityTypeId", facilityTypeId);
    if (null != fullSupply) {
      query.setParameter("fullSupply", fullSupply);
    }
    if (null != programId) {
      query.setParameter("programId", programId);
    }
    if (!isEmpty(orderableIds)) {
      query.setParameter("orderableIds", orderableIds);
    }

    return query;
  }

  private <T> CriteriaQuery<T> prepareQuery(String facilityTypeCode,
                                            String programCode,
                                            CriteriaQuery<T> query,
                                            boolean count) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();

    Root<FacilityTypeApprovedProduct> ftap = query.from(FacilityTypeApprovedProduct.class);

    Join<FacilityTypeApprovedProduct, FacilityType> ft = ftap.join(FACILITY_TYPE);
    Join<FacilityTypeApprovedProduct, Program> program = ftap.join(PROGRAM);

    Predicate conjunctionPredicate = builder.conjunction();
    if (StringUtils.isNotBlank(programCode)) {
      conjunctionPredicate = builder.and(conjunctionPredicate,
          builder.equal(program.get(CODE), Code.code(programCode)));
    }
    conjunctionPredicate = builder.and(conjunctionPredicate,
        builder.equal(ft.get(CODE), facilityTypeCode));
    Join<FacilityTypeApprovedProduct, Orderable> orderable = ftap.join("orderable");
    Join<Orderable, Set<ProgramOrderable>> programOrderables =
        orderable.joinSet("programOrderables");

    conjunctionPredicate = builder.and(conjunctionPredicate,
        builder.isTrue(programOrderables.get("active")));
    conjunctionPredicate = builder.and(conjunctionPredicate,
        builder.equal(programOrderables.get(PROGRAM), program));

    if (count) {
      CriteriaQuery<Long> countQuery = (CriteriaQuery<Long>) query;
      query = (CriteriaQuery<T>) countQuery.select(builder.count(ftap));
    } else {
      CriteriaQuery<FacilityTypeApprovedProduct> ftapQuery =
          (CriteriaQuery<FacilityTypeApprovedProduct>) query;
      query = (CriteriaQuery<T>) ftapQuery.select(ftap);
    }
    query.where(conjunctionPredicate);

    if (!count) {
      Join<ProgramOrderable, OrderableDisplayCategory> category =
          programOrderables.join("orderableDisplayCategory");

      query.orderBy(
          builder.asc(category.get(ORDERED_DISPLAY_VALUE).get("displayOrder")),
          builder.asc(category.get(ORDERED_DISPLAY_VALUE).get("displayName")),
          builder.asc(orderable.get("productCode"))
      );
    }

    return query;
  }
}
