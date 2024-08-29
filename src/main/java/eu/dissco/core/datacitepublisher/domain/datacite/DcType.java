package eu.dissco.core.datacitepublisher.domain.datacite;


import static eu.dissco.core.datacitepublisher.properties.DoiProperties.RESOURCE_TYPE_GENERAL;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@Setter(value = AccessLevel.PACKAGE)
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DcType {

  private String resourceType;
  private final String resourceTypeGeneral = RESOURCE_TYPE_GENERAL;
}
