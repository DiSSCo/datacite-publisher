package eu.dissco.core.datacitepublisher.domain.datacite;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DcRelatedIdentifiers {
  private String relationType;
  private String relatedIdentifier;
  private String relatedIdentifierType;

  public DcRelatedIdentifiers withRelationType(String s){
    this.relationType = s;
    return this;
  }

  public DcRelatedIdentifiers withRelatedIdentifier(String s){
    this.relatedIdentifier = s;
    return this;
  }

  public DcRelatedIdentifiers withRelatedIdentifierType(String s){
    this.relatedIdentifierType = s;
    return this;
  }

}
