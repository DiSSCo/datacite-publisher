package eu.dissco.core.datacitepublisher.domain.datacite;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DcNameIdentifiers {

  private String schemeUri;
  private String nameIdentifier;
  private String nameIdentifierScheme;
  
  public DcNameIdentifiers withSchemeUri(String s){
    this.schemeUri = s;
    return this;
  }

  public DcNameIdentifiers withNameIdentifier(String s){
    this.nameIdentifier = s;
    return this;
  }

  public DcNameIdentifiers withNameIdentifierScheme(String s){
    this.nameIdentifierScheme = s;
    return this;
  }
}
