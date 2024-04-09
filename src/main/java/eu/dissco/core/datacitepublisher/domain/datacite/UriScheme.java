package eu.dissco.core.datacitepublisher.domain.datacite;

public enum UriScheme {
  ROR("https://ror.org", "ROR"),
  HANDLE("https://hdl.handle.net", "Handle"),
  DOI("https://doi.org", "DOI"),
  QID(null, "Q Number");

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
    return UriScheme.QID;
  }

  public String getUri() {
    return uri;
  }

  public String getSchemeName() {
    return schemeName;
  }

}
