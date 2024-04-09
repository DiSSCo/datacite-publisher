package eu.dissco.core.datacitepublisher.domain.datacite;

import lombok.Value;

@Value
public class DcPublisher {
  String name = "Distributed System of Scientific Collections";
  String publisherIdentifier = "https://ror.org/0566bfb96";
  String publisherIdentifierScheme = UriScheme.ROR.getSchemeName();
  String schemeUri = UriScheme.ROR.getUri();
}
