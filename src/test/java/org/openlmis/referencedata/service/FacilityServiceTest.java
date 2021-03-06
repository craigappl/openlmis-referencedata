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

package org.openlmis.referencedata.service;

import static com.google.common.collect.ImmutableSet.of;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("PMD.TooManyMethods")
public class FacilityServiceTest {

  private static final String CODE = "code";
  private static final String NAME = "name";
  private static final String FACILITY_TYPE_CODE = "type";
  private static final String ZONE_ID = "zoneId";
  private static final String RECURSE = "recurse";
  private static final String FACILITY_NAME = "Facility";
  private static final String FACILITY_TYPE = "facility-type";
  private static final String FACILITY_CODE = "FAC1";

  @Mock
  private GeographicZoneRepository geographicZoneRepository;

  @Mock
  private GeographicZoneService geographicZoneService;

  @Mock
  private FacilityRepository facilityRepository;

  @Mock
  private FacilityTypeRepository facilityTypeRepository;

  @Mock
  private Facility facility;

  @Mock
  private Facility facility2;

  private UUID facility1Id = UUID.randomUUID();
  private UUID facility2Id = UUID.randomUUID();
  private UUID parentId = UUID.randomUUID();
  private UUID childId = UUID.randomUUID();
  private UUID childOfChildId = UUID.randomUUID();
  private List<Facility> facilityList;

  @InjectMocks
  private FacilityService facilityService = new FacilityService();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    facilityList = Lists.newArrayList(facility, facility2);
    when(facility.getId()).thenReturn(facility1Id);
    when(facility2.getId()).thenReturn(facility2Id);
  }

  @Test
  public void shouldReturnAllIfZoneCodeAndNameNotProvidedForSearch() {
    when(facilityRepository.findAll()).thenReturn(facilityList);

    Map<String, Object> searchParams = new HashMap<>();
    searchParams.put(RECURSE, false);
    List<Facility> actual = facilityService.searchFacilities(searchParams);

    verify(facilityRepository).findAll();
    assertEquals(facilityList, actual);

  }

  @Test
  public void shouldOnlyThrowValidationExceptionIfQueryMapCantBeParsed() {
    when(facilityRepository.findAll()).thenReturn(facilityList);

    Map<String, Object> searchParams = new HashMap<>();
    searchParams.put(CODE, "-1");
    searchParams.put(NAME, "-1");
    searchParams.put(ZONE_ID, "a");
    searchParams.put(RECURSE, "a");
    facilityService.searchFacilities(searchParams);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfGeographicZoneDoesNotExist() {
    when(geographicZoneRepository.findOne(any(UUID.class))).thenReturn(null);

    Map<String, Object> searchParams = new HashMap<>();
    searchParams.put(ZONE_ID, UUID.randomUUID());
    facilityService.searchFacilities(searchParams);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfFacilityTypeDoesNotExist() {
    when(facilityTypeRepository.findOneByCode(any(String.class))).thenReturn(null);

    Map<String, Object> searchParams = new HashMap<>();
    searchParams.put(ZONE_ID, UUID.randomUUID());
    facilityService.searchFacilities(searchParams);
  }

  @Test
  public void shouldReturnAllElementsWhenNoSearchCriteriaProvided() {
    when(facilityRepository.findAll()).thenReturn(facilityList);

    List<Facility> actual = facilityService.getFacilities(new LinkedMultiValueMap<>());
    verify(facilityRepository).findAll();
    assertEquals(facilityList, actual);
  }

  @Test
  public void shouldReturnByIdsWhenIdProvidedInQueryMap() {
    when(facilityRepository.findAll(anyCollectionOf(UUID.class))).thenReturn(facilityList);

    MultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    UUID uuid = UUID.randomUUID();
    queryMap.add("id", uuid.toString());
    List<Facility> actual = facilityService.getFacilities(queryMap);
    verify(facilityRepository).findAll(Sets.newHashSet(uuid));
    assertEquals(facilityList, actual);
  }

  @Test
  public void shouldReturnByIdsWhenIdsProvidedInQueryMap() {
    when(facilityRepository.findAll(anyCollectionOf(UUID.class))).thenReturn(facilityList);

    MultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    UUID uuid = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();
    queryMap.add("id", uuid.toString());
    queryMap.add("id", uuid2.toString());
    List<Facility> actual = facilityService.getFacilities(queryMap);
    verify(facilityRepository).findAll(Sets.newHashSet(uuid, uuid2));
    assertEquals(facilityList, actual);
  }

  @Test
  public void shouldFindFacilitiesInChildZonesIfRecurseOptionProvided() {
    prepareForSearchWithRecurse();

    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add(RECURSE, true);
    params.add(CODE, FACILITY_CODE);
    params.add(NAME, FACILITY_NAME);
    params.add(FACILITY_TYPE_CODE, FACILITY_TYPE);
    params.add(ZONE_ID, parentId);

    final List<Facility> actual = facilityService.getFacilities(params);

    verifyAfterSearchWithRecurse(actual);
  }

  @Test
  public void shouldFindFacilitiesInParentZoneOnlyIfRecurseOptionIsOff() {
    prepareForSearchWithoutRecurse();

    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add(RECURSE, false);
    params.add(CODE, FACILITY_CODE);
    params.add(NAME, FACILITY_NAME);
    params.add(FACILITY_TYPE_CODE, FACILITY_TYPE);
    params.add(ZONE_ID, parentId);

    List<Facility> actual = facilityService.getFacilities(params);

    verifyAfterSearchWithoutRecurse(actual);
  }

  @Test
  public void shouldReturnAllElementsIfNoSearchCriteriaProvided() {
    when(facilityRepository.findAll()).thenReturn(facilityList);

    List<Facility> actual = facilityService.searchFacilities(new HashMap<>());
    verify(facilityRepository).findAll();
    assertEquals(facilityList, actual);
  }

  @Test
  public void shouldSearchForFacilitiesInChildZonesIfRecurseOptionProvided() {
    prepareForSearchWithRecurse();

    Map<String, Object> params = new HashMap<>();
    params.put(RECURSE, true);
    params.put(CODE, FACILITY_CODE);
    params.put(NAME, FACILITY_NAME);
    params.put(FACILITY_TYPE_CODE, FACILITY_TYPE);
    params.put(ZONE_ID, parentId);

    final List<Facility> actual = facilityService.searchFacilities(params);

    verifyAfterSearchWithRecurse(actual);
  }

  @Test
  public void shouldSearchForFacilitiesInParentZoneOnlyIfRecurseOptionIsOff() {
    prepareForSearchWithoutRecurse();

    Map<String, Object> params = new HashMap<>();
    params.put(RECURSE, false);
    params.put(CODE, FACILITY_CODE);
    params.put(NAME, FACILITY_NAME);
    params.put(FACILITY_TYPE_CODE, FACILITY_TYPE);
    params.put(ZONE_ID, parentId);

    List<Facility> actual = facilityService.searchFacilities(params);

    verifyAfterSearchWithoutRecurse(actual);
  }

  @Test
  public void shouldSearchForFacilitiesWithExtraData() {
    final String code = "FAC1";
    final String name = "Facility";

    when(geographicZoneRepository.exists(parentId)).thenReturn(true);
    when(geographicZoneService.getAllZonesInHierarchy(parentId))
        .thenReturn(Sets.newHashSet(childId, childOfChildId));

    when(facilityTypeRepository.existsByCode(FACILITY_TYPE)).thenReturn(true);

    when(facilityRepository
        .search(code, name, of(parentId, childId, childOfChildId),
            FACILITY_TYPE, "{\"type\":\"rural\"}"))
        .thenReturn(Lists.newArrayList(facility2));

    Map<String, String> extraData = new HashMap<>();
    extraData.put("type", "rural");

    Map<String, Object> params = new HashMap<>();
    params.put(RECURSE, true);
    params.put(CODE, code);
    params.put(NAME, name);
    params.put(FACILITY_TYPE_CODE, FACILITY_TYPE);
    params.put(ZONE_ID, parentId);
    params.put("extraData", extraData);

    List<Facility> actual = facilityService.searchFacilities(params);

    assertEquals(1, actual.size());
    assertThat(actual, hasItem(facility2));
  }

  private void prepareForSearchWithRecurse() {

    when(geographicZoneRepository.exists(parentId)).thenReturn(true);
    when(geographicZoneService.getAllZonesInHierarchy(parentId))
        .thenReturn(Sets.newHashSet(childId, childOfChildId));

    when(facilityTypeRepository.existsByCode(FACILITY_TYPE)).thenReturn(true);

    when(facilityRepository
        .search(
            FACILITY_CODE, FACILITY_NAME,
            of(parentId, childId, childOfChildId), FACILITY_TYPE, null))
        .thenReturn(Lists.newArrayList(facility, facility2));
  }

  private void verifyAfterSearchWithRecurse(List<Facility> actual) {
    verify(facilityRepository)
        .search(FACILITY_CODE, FACILITY_NAME,
            of(parentId, childId, childOfChildId), FACILITY_TYPE, null);

    assertEquals(2, actual.size());
    assertThat(actual, hasItem(facility));
    assertThat(actual, hasItem(facility2));
  }

  private void prepareForSearchWithoutRecurse() {
    when(geographicZoneRepository.exists(parentId)).thenReturn(true);

    when(facilityTypeRepository.existsByCode(FACILITY_TYPE)).thenReturn(true);

    when(facilityRepository
        .search(FACILITY_CODE, FACILITY_NAME, of(parentId), FACILITY_TYPE, null))
        .thenReturn(Lists.newArrayList(facility));
  }

  private void verifyAfterSearchWithoutRecurse(List<Facility> actual) {
    verify(facilityRepository)
        .search(FACILITY_CODE, FACILITY_NAME, of(parentId), FACILITY_TYPE, null);
    verifyNoMoreInteractions(facilityRepository);

    assertEquals(1, actual.size());
    assertThat(actual, hasItem(facility));
  }
}
