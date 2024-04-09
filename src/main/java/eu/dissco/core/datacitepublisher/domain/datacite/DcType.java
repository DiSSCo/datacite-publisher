package eu.dissco.core.datacitepublisher.domain.datacite;

import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.RESOURCE_TYPE_GENERAL;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;

@Getter
@NoArgsConstructor
public class DcType {

  private String resourceType;
  private final String resourceTypeGeneral = RESOURCE_TYPE_GENERAL;

  public DcType(String resourceType){
    this.resourceType = resourceType;
  }

}
