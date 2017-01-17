package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.dto.SupervisoryNodeDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.service.RequisitionGroupProgramScheduleService;
import org.openlmis.referencedata.util.messagekeys.FacilityMessageKeys;
import org.openlmis.referencedata.util.messagekeys.ProgramMessageKeys;
import org.openlmis.referencedata.util.messagekeys.SupervisoryNodeMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Controller
public class SupervisoryNodeController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(SupervisoryNodeController.class);

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  private RequisitionGroupProgramScheduleService requisitionGroupProgramScheduleService;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private ProgramRepository programRepository;

  /**
   * Allows creating new supervisoryNode. If the id is specified, it will be ignored.
   *
   * @param supervisoryNodeDto A supervisoryNodeDto bound to the request body
   * @return ResponseEntity containing the created supervisoryNode
   */
  @RequestMapping(value = "/supervisoryNodes", method = RequestMethod.POST)
  public ResponseEntity<?> createSupervisoryNode(
      @RequestBody SupervisoryNodeDto supervisoryNodeDto) {
    LOGGER.debug("Creating new supervisoryNode");
    supervisoryNodeDto.setId(null);
    SupervisoryNode supervisoryNode = SupervisoryNode.newSupervisoryNode(supervisoryNodeDto);
    supervisoryNodeRepository.save(supervisoryNode);
    LOGGER.debug("Created new supervisoryNode with id: " + supervisoryNode.getId());
    return new ResponseEntity<>(exportToDto(supervisoryNode), HttpStatus.CREATED);
  }

  /**
   * Get all supervisoryNodes.
   *
   * @return SupervisoryNodeDtos.
   */
  @RequestMapping(value = "/supervisoryNodes", method = RequestMethod.GET)
  public ResponseEntity<?> getAllSupervisoryNodes() {
    Iterable<SupervisoryNode> supervisoryNodes = supervisoryNodeRepository.findAll();
    List<SupervisoryNodeDto> supervisoryNodeDtos = new ArrayList<>();

    for (SupervisoryNode supervisoryNode : supervisoryNodes) {
      supervisoryNodeDtos.add(exportToDto(supervisoryNode));
    }

    return new ResponseEntity<>(supervisoryNodeDtos, HttpStatus.OK);
  }

  /**
   * Get chosen supervisoryNode.
   *
   * @param supervisoryNodeId UUID of supervisoryNode whose we want to get
   * @return SupervisoryNode.
   */
  @RequestMapping(value = "/supervisoryNodes/{id}", method = RequestMethod.GET)
  public ResponseEntity<?> getSupervisoryNode(@PathVariable("id") UUID supervisoryNodeId) {
    SupervisoryNode supervisoryNode = supervisoryNodeRepository.findOne(supervisoryNodeId);
    if (supervisoryNode == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(exportToDto(supervisoryNode), HttpStatus.OK);
    }
  }

  /**
   * Allows updating supervisoryNode.
   *
   * @param supervisoryNodeDto A supervisoryNodeDto bound to the request body
   * @param supervisoryNodeId UUID of supervisoryNode which we want to update
   * @return ResponseEntity containing the updated supervisoryNode
   */
  @RequestMapping(value = "/supervisoryNodes/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateSupervisoryNode(@RequestBody SupervisoryNodeDto supervisoryNodeDto,
                                                 @PathVariable("id") UUID supervisoryNodeId) {
    LOGGER.debug("Updating supervisoryNode with id: " + supervisoryNodeId);

    SupervisoryNode supervisoryNodeToUpdate =
        supervisoryNodeRepository.findOne(supervisoryNodeId);

    if (supervisoryNodeToUpdate == null) {
      supervisoryNodeToUpdate = new SupervisoryNode();
    }

    supervisoryNodeToUpdate.updateFrom(SupervisoryNode.newSupervisoryNode(supervisoryNodeDto));
    supervisoryNodeRepository.save(supervisoryNodeToUpdate);

    LOGGER.debug("Updated supervisoryNode with id: " + supervisoryNodeId);
    return new ResponseEntity<>(exportToDto(supervisoryNodeToUpdate), HttpStatus.OK);
  }

  /**
   * Allows deleting supervisoryNode.
   *
   * @param supervisoryNodeId UUID of supervisoryNode whose we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/supervisoryNodes/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteSupervisoryNode(@PathVariable("id") UUID supervisoryNodeId) {
    SupervisoryNode supervisoryNode = supervisoryNodeRepository.findOne(supervisoryNodeId);
    if (supervisoryNode == null) {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    } else {
      supervisoryNodeRepository.delete(supervisoryNode);
      return new ResponseEntity<SupervisoryNode>(HttpStatus.NO_CONTENT);
    }
  }


  /**
   * Searching for supervisoryNode with given parameters.
   *
   * @param programId UUID of RequisitionGroup program by which we want to search
   * @param facilityId UUID of RequisitionGroup facility by which we want to search
   * @return Found supervisoryNode
   */
  @RequestMapping(value = "/supervisoryNodes/search", method = RequestMethod.GET)
  public ResponseEntity<?> findSupervisoryNodeByRequisitionGroupFacilityAndProgram(
      @RequestParam("programId") UUID programId, @RequestParam("facilityId") UUID facilityId) {

    Facility facility = facilityRepository.findOne(facilityId);
    Program program = programRepository.findOne(programId);

    if (program == null) {
      throw new ValidationMessageException(ProgramMessageKeys.ERROR_NOT_FOUND);
    }

    if (facility == null) {
      throw new ValidationMessageException(FacilityMessageKeys.FACILITY_NOT_FOUND);
    }

    RequisitionGroupProgramSchedule foundGroup = requisitionGroupProgramScheduleService
            .searchRequisitionGroupProgramSchedule(program, facility);

    if (foundGroup == null) {
      final Object[] errorArgs = {programId, facility};
      return ResponseEntity
          .status(HttpStatus.NOT_FOUND)
          .body(buildErrorResponse(
              SupervisoryNodeMessageKeys.ERROR_NOT_FOUND_WITH_PROGRAM_AND_FACILITY,
              errorArgs));
    }

    SupervisoryNode result = foundGroup.getRequisitionGroup().getSupervisoryNode();

    return ResponseEntity.ok(Collections.singletonList(exportToDto(result)));
  }

  private SupervisoryNodeDto exportToDto(SupervisoryNode supervisoryNode) {
    SupervisoryNodeDto supervisoryNodeDto = null;

    if (supervisoryNode != null) {
      supervisoryNodeDto = new SupervisoryNodeDto();
      supervisoryNode.export(supervisoryNodeDto);
    }

    return supervisoryNodeDto;
  }
}
