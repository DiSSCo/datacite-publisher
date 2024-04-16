package eu.dissco.core.datacitepublisher.domain.datacite;

import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.RESOURCE_TYPE_GENERAL;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@AllArgsConstructor
@Setter(value = AccessLevel.PACKAGE)
@Getter
public class DcType {

  private String resourceType;
  private final String resourceTypeGeneral = RESOURCE_TYPE_GENERAL;
}
