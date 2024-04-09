package eu.dissco.core.datacitepublisher.domain.datacite;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DcAlternateIdentifier {

  private String alternateIdentifierType;
  private String alternateIdentifier;

  public DcAlternateIdentifier withAlternateIdentifierType(String s){
    this.alternateIdentifierType = s;
    return this;
  }

  public DcAlternateIdentifier withAlternateIdentifier(String s){
    this.alternateIdentifier = s;
    return this;
  }

}
