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
public class DcRelatedIdentifiers {
  private RelationType relationType;
  private String relatedIdentifier;
  private String relatedIdentifierType;
}
