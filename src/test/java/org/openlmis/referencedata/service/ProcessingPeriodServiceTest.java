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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProcessingPeriodRepository;
import org.openlmis.referencedata.repository.ProcessingScheduleRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.RequisitionGroupProgramScheduleRepository;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.ProcessingPeriodDataBuilder;
import org.openlmis.referencedata.testbuilder.ProcessingScheduleDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@RunWith(MockitoJUnitRunner.class)
public class ProcessingPeriodServiceTest {

  @InjectMocks
  private ProcessingPeriodService periodService;

  @Mock
  private ProcessingPeriodRepository periodRepository;

  @Mock
  private RequisitionGroupProgramScheduleRepository repository;

  @Mock
  private ProgramRepository programRepository;

  @Mock
  private FacilityRepository facilityRepository;

  @Mock
  private ProcessingScheduleRepository processingScheduleRepository;

  @Mock
  private RequisitionGroupProgramSchedule requisitionGroupProgramSchedule;

  private ProcessingPeriod period = new ProcessingPeriodDataBuilder().build();
  private ProcessingSchedule schedule = new ProcessingScheduleDataBuilder().build();
  private Program program = new ProgramDataBuilder().build();
  private Facility facility = new FacilityDataBuilder().build();
  private List<ProcessingPeriod> periods = generateInstances();
  private PageRequest pageable = new PageRequest(0, 10);

  @Before
  public void setUp() {
    when(requisitionGroupProgramSchedule.getProcessingSchedule()).thenReturn(schedule);
    when(facilityRepository.findOne(facility.getId())).thenReturn(facility);
    when(programRepository.findOne(program.getId())).thenReturn(program);
    when(processingScheduleRepository.findOne(schedule.getId())).thenReturn(schedule);
  }

  @Test(expected = NotFoundException.class)
  public void shouldThrowExceptionWhenProgramWasNotFoundById() {
    ProcessingPeriodSearchParams params = new ProcessingPeriodSearchParams(
        program.getId(), facility.getId(), null, null);
    when(programRepository.findOne(program.getId())).thenReturn(null);
    periodService.searchPeriods(params, pageable);
  }

  @Test(expected = NotFoundException.class)
  public void shouldThrowExceptionWhenFacilityWasNotFoundById() {
    ProcessingPeriodSearchParams params = new ProcessingPeriodSearchParams(
        program.getId(), facility.getId(), null, null);
    when(facilityRepository.findOne(facility.getId())).thenReturn(null);
    periodService.searchPeriods(params, pageable);
  }

  @Test
  public void shouldReturnEmptyPageWhenScheduleWasNotFoundForFacilityAndProgram() {
    ProcessingPeriodSearchParams params = new ProcessingPeriodSearchParams(
        program.getId(), facility.getId(), null, null);
    doReturn(Collections.emptyList()).when(repository)
        .searchRequisitionGroupProgramSchedules(program, facility);
    Page<ProcessingPeriod> result = periodService.searchPeriods(params, pageable);
    assertEquals(0, result.getContent().size());
  }

  @Test
  public void shouldFindPeriodsByProgramAndFacility() {
    doReturn(Collections.singletonList(requisitionGroupProgramSchedule)).when(repository)
          .searchRequisitionGroupProgramSchedules(program, facility);
    doReturn(Pagination.getPage(Collections.singletonList(period), pageable, 1))
        .when(periodRepository).findByProcessingSchedule(schedule, pageable);

    ProcessingPeriodSearchParams params = new ProcessingPeriodSearchParams(
        program.getId(), facility.getId(), null, null);

    periodService.searchPeriods(params, pageable);

    verify(repository).searchRequisitionGroupProgramSchedules(program, facility);
    verify(periodRepository).findByProcessingSchedule(schedule, pageable);
  }

  @Test(expected = NotFoundException.class)
  public void shouldThrowExceptionWhenScheduleWasNotFoundById() {
    doReturn(null).when(processingScheduleRepository).findOne(schedule.getId());

    ProcessingPeriodSearchParams params = new ProcessingPeriodSearchParams(
        null, null, schedule.getId(), null);

    periodService.searchPeriods(params, pageable);
  }

  @Test
  public void shouldFindPeriodsBySchedule() {
    doReturn(Pagination.getPage(Collections.singletonList(period), pageable, 1))
        .when(periodRepository).findByProcessingSchedule(schedule, pageable);

    ProcessingPeriodSearchParams params = new ProcessingPeriodSearchParams(
        null, null, schedule.getId(), null);

    periodService.searchPeriods(params, pageable);

    verify(periodRepository).findByProcessingSchedule(schedule, pageable);
  }

  @Test
  public void shouldFindPeriodsWithinProvidedDateIfTheyExist() {
    List<ProcessingPeriod> matchedPeriods = new ArrayList<>();
    matchedPeriods.addAll(periods);
    matchedPeriods.remove(periods.get(0));

    ProcessingPeriodSearchParams params = new ProcessingPeriodSearchParams(
        null, null, schedule.getId(), periods.get(0).getStartDate());

    when(periodRepository
        .findByProcessingScheduleAndStartDateLessThanEqual(
            schedule,
            periods.get(0).getStartDate(),
            pageable))
        .thenReturn(Pagination.getPage(matchedPeriods, pageable, 5));

    Page<ProcessingPeriod> receivedPeriods = periodService
        .searchPeriods(params, pageable);

    assertEquals(4, receivedPeriods.getContent().size());
    for (ProcessingPeriod period : receivedPeriods.getContent()) {
      assertEquals(schedule, period.getProcessingSchedule());
      assertTrue(periods.get(0).getStartDate().isAfter(period.getStartDate()));
    }
  }

  private List<ProcessingPeriod> generateInstances() {
    return IntStream
        .range(0, 5)
        .mapToObj(this::generatePeriod)
        .collect(Collectors.toList());
  }

  private ProcessingPeriod generatePeriod(int instanceNumber) {
    LocalDate now = LocalDate.now();

    return new ProcessingPeriodDataBuilder()
        .withSchedule(schedule)
        .withPeriod(now.minusDays(instanceNumber), now.plusDays(instanceNumber))
        .build();
  }

}
