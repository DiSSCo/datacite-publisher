package eu.dissco.core.datacitepublisher.domain.datacite;


import lombok.Value;

@Value
public class DcPublisher {
  String name;
  String publisherIdentifier;
  String publisherIdentifierScheme;
  String schemeUri;

}
