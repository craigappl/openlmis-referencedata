package org.openlmis.referencedata.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.domain.SupervisoryNode;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
public class RequisitionGroupBaseDto extends BaseDto implements RequisitionGroup.Importer,
    RequisitionGroup.Exporter {

  @Getter
  @Setter
  private String code;

  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private String description;

  @JsonProperty
  @Getter
  private SupervisoryNodeBaseDto supervisoryNode;

  public RequisitionGroupBaseDto(UUID id) {
    setId(id);
  }

  @JsonIgnore
  @Override
  public void setSupervisoryNode(SupervisoryNode supervisoryNode) {
    if (supervisoryNode != null) {
      this.supervisoryNode = new SupervisoryNodeDto(supervisoryNode.getId());
    } else {
      this.supervisoryNode = null;
    }
  }

  public void setSupervisoryNode(SupervisoryNodeBaseDto supervisoryNode) {
    this.supervisoryNode = supervisoryNode;
  }

  @Override
  public List<RequisitionGroupProgramSchedule.Importer> getRequisitionGroupProgramSchedules() {
    return null;
  }

  @JsonIgnore
  @Override
  public void setRequisitionGroupProgramSchedules(
      List<RequisitionGroupProgramSchedule> schedules) {}

  @Override
  public Set<Facility.Importer> getMemberFacilities() {
    return null;
  }

  @JsonIgnore
  @Override
  public void setMemberFacilities(Set<Facility> memberFacilities) {}

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RequisitionGroupBaseDto)) {
      return false;
    }
    RequisitionGroupBaseDto that = (RequisitionGroupBaseDto) obj;
    return Objects.equals(id, that.id) && Objects.equals(code, that.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, code);
  }
}
