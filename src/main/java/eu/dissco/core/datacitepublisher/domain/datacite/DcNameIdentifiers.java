package eu.dissco.core.datacitepublisher.domain.datacite;

public record DcNameIdentifiers(
    String schemeUri,
    String nameIdentifier,
    String nameIdentifierScheme
) {

}
