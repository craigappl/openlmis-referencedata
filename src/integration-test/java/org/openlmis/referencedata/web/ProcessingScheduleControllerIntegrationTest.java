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

package org.openlmis.referencedata.web;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import guru.nidi.ramltester.junit.RamlMatchers;
import org.junit.Test;
import org.openlmis.referencedata.PageImplRepresentation;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.ProcessingScheduleDto;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.UUID;

@SuppressWarnings({"PMD.TooManyMethods"})
public class ProcessingScheduleControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/processingSchedules";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";

  private ProcessingSchedule schedule;
  private Facility facility;
  private Program program;
  private RequisitionGroupProgramSchedule requisitionGroupProgramSchedule;
  private UUID processingScheduleId;
  private UUID facilityId;
  private UUID programId;

  /**
   * Test class constructor.
   */
  public ProcessingScheduleControllerIntegrationTest() {
    schedule = new ProcessingSchedule(Code.code("PS1"), "Schedule1");
    schedule.setId(UUID.randomUUID());
    program = new Program("PRO-1");
    facility = new Facility("FAC-1");
    requisitionGroupProgramSchedule = new RequisitionGroupProgramSchedule();
    requisitionGroupProgramSchedule.setProcessingSchedule(schedule);
    processingScheduleId = UUID.randomUUID();
    facilityId = UUID.randomUUID();
    programId = UUID.randomUUID();
  }

  @Test
  public void shouldDeleteSchedule() {
    mockUserHasRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    given(scheduleRepository.findOne(processingScheduleId)).willReturn(schedule);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", processingScheduleId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostProcessingSchedule() {
    mockUserHasRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    ProcessingSchedule response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(schedule)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(ProcessingSchedule.class);

    assertEquals(schedule, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutProcessingSchedule() {
    mockUserHasRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    schedule.setDescription("OpenLMIS");
    given(scheduleRepository.findOne(processingScheduleId)).willReturn(schedule);

    ProcessingSchedule response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", processingScheduleId)
        .body(schedule)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(ProcessingSchedule.class);

    assertEquals(schedule, response);
    assertEquals("OpenLMIS", response.getDescription());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllProcessingSchedules() {
    ProcessingScheduleDto dto = new ProcessingScheduleDto();
    schedule.export(dto);
    PageRequest pageRequest = new PageRequest(1, 10);
    given(scheduleRepository.findAll(pageRequest))
        .willReturn(Pagination.getPage(Collections.singletonList(schedule), pageRequest, 11));

    PageImplRepresentation<LinkedHashMap> response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam("page", 1)
        .queryParam("size", 10)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    assertEquals(1, response.getContent().size());
    assertEquals(dto.getId().toString(), response.getContent().get(0).get("id").toString());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetProcessingSchedule() {

    given(scheduleRepository.findOne(processingScheduleId)).willReturn(schedule);

    ProcessingSchedule response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", processingScheduleId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(ProcessingSchedule.class);

    assertEquals(schedule, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetProcessingScheduleByFacilityAndProgram() {

    given(facilityRepository.findOne(facilityId)).willReturn(facility);
    given(programRepository.findOne(programId)).willReturn(program);
    given(requisitionGroupProgramScheduleService.searchRequisitionGroupProgramSchedules(
        program, facility)).willReturn(singletonList(requisitionGroupProgramSchedule));

    ProcessingSchedule[] response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam("facilityId", facilityId)
        .queryParam("programId", programId)
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(ProcessingSchedule[].class);

    assertEquals(schedule, response[0]);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldBadRequestSearchWhenProgramDoesNotExist() {

    given(facilityRepository.findOne(facilityId)).willReturn(facility);
    given(programRepository.findOne(programId)).willReturn(null);
    given(requisitionGroupProgramScheduleService.searchRequisitionGroupProgramSchedules(
        program, facility)).willReturn(singletonList(requisitionGroupProgramSchedule));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam("facilityId", facilityId)
        .queryParam("programId", programId)
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldBadRequestSearchWhenFacilityDoesNotExist() {

    given(facilityRepository.findOne(facilityId)).willReturn(null);
    given(programRepository.findOne(programId)).willReturn(program);
    given(requisitionGroupProgramScheduleService.searchRequisitionGroupProgramSchedules(
        program, facility)).willReturn(singletonList(requisitionGroupProgramSchedule));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam("facilityId", facilityId)
        .queryParam("programId", programId)
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedOnDeleteScheduleIfUserHasNoRight() {
    mockUserHasNoRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", processingScheduleId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedOnPostProcessingScheduleIfUserHasNoRight() {
    mockUserHasNoRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(schedule)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedOnPutProcessingScheduleIfUserHasNoRight() {
    mockUserHasNoRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", processingScheduleId)
        .body(schedule)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAllSchedulesShouldReturnUnauthorizedWithoutAuthorization() {

    restAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(401);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getChosenScheduleShouldReturnUnauthorizedWithoutAuthorization() {

    restAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", processingScheduleId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(401);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnNotFoundIfEntityDoesNotExist() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    given(scheduleRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnUnauthorizedIfUserDoesNotHaveRight() {
    doThrow(new UnauthorizedException(new Message("UNAUTHORIZED")))
        .when(rightService)
        .checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    given(scheduleRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.unauthorized(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAuditLog() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    given(scheduleRepository.findOne(any(UUID.class))).willReturn(schedule);

    AuditLogHelper.ok(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
