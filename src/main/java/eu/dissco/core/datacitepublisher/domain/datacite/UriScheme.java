package eu.dissco.core.datacitepublisher.domain.datacite;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum UriScheme {
  ROR("https://ror.org", "ROR"),
  HANDLE("https://hdl.handle.net", "Handle"),
  DOI("https://doi.org", "DOI"),
  QID("https://www.wikidata.org/", "Q Number");

  final String uri;
  final String schemeName;

  private UriScheme(String uri, String schemeName) {
    this.uri = uri;
    this.schemeName = schemeName;
  }

  public static UriScheme determineScheme(String identifier) {
    if (identifier.contains("ror")) {
      return UriScheme.ROR;
    }
    if (identifier.contains("hdl")) {
      return UriScheme.HANDLE;
    }
    if (identifier.contains("doi")) {
      return UriScheme.DOI;
    }
    if (identifier.contains("wikidata")) {
      return UriScheme.QID;
    }
    log.error("Invalid identifier: {} can not be matched to identifier scheme", identifier);
    throw new IllegalStateException();
  }

  public String getUri() {
    return uri;
  }

  public String getSchemeName() {
    return schemeName;
  }

}
