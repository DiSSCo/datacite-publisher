package eu.dissco.core.datacitepublisher.domain.datacite;

import lombok.Value;

@Value
public class DcPublisher {
  String name;
  String publisherIdentifier;
  String publisherIdentifierScheme;
  String schemeUri;

  public DcPublisher(){
    name = "Distributed System of Scientific Collections";
    publisherIdentifier  = "https://ror.org/0566bfb96";
    publisherIdentifierScheme = UriScheme.ROR.getSchemeName();
    schemeUri = UriScheme.ROR.getUri();
  }

}
