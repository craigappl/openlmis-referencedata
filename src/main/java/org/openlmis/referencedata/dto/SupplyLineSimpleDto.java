package org.openlmis.referencedata.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
public class SupplyLineSimpleDto extends BaseDto implements SupplyLine.Exporter,
    SupplyLine.Importer {

  @JsonProperty
  private UUID supervisoryNode;

  @Getter
  @Setter
  private String description;

  @JsonProperty
  private UUID program;

  @JsonProperty
  private UUID supplyingFacility;

  @Getter
  @Setter
  private Boolean exportOrders;

  @JsonIgnore
  @Override
  public SupervisoryNode.Importer getSupervisoryNode() {
    if (supervisoryNode != null) {
      return new SupervisoryNodeBaseDto(supervisoryNode);
    }

    return null;
  }

  @JsonIgnore
  @Override
  public void setSupervisoryNode(SupervisoryNode supervisoryNode) {
    if (supervisoryNode != null) {
      this.supervisoryNode = supervisoryNode.getId();
    } else {
      this.supervisoryNode = null;
    }
  }

  @JsonIgnore
  @Override
  public Program.Importer getProgram() {
    if (program != null) {
      return new ProgramDto(program);
    }

    return null;
  }

  @JsonIgnore
  @Override
  public void setProgram(Program program) {
    if (program != null) {
      this.program = program.getId();
    } else {
      this.program = null;
    }
  }

  @JsonIgnore
  @Override
  public Facility.Importer getSupplyingFacility() {
    if (supplyingFacility != null) {
      return new FacilityDto(supplyingFacility);
    }

    return null;
  }

  @JsonIgnore
  @Override
  public void setSupplyingFacility(Facility supplyingFacility) {
    if (supplyingFacility != null) {
      this.supplyingFacility = supplyingFacility.getId();
    } else {
      this.supplyingFacility = null;
    }
  }
}
