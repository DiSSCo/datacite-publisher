package eu.dissco.core.datacitepublisher.domain.datacite;

import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.RESOURCE_TYPE_GENERAL;

import lombok.Value;

@Value
public class DcType {

  String resourceType;
  String resourceTypeGeneral = RESOURCE_TYPE_GENERAL;

  public DcType(String resourceType){
    this.resourceType = resourceType;
  }

}
