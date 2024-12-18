package eu.dissco.core.datacitepublisher.domain.datacite;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@AllArgsConstructor
@Setter(value = AccessLevel.PACKAGE)
@Getter
public class DcRights {

  private String rights;
  private String rightsUri;
  private String schemeUri;
  private String rightsIdentifier;
  private String rightsIdentifierScheme;
  private String lang;

}
