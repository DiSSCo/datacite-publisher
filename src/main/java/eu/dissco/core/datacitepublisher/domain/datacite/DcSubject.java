package eu.dissco.core.datacitepublisher.domain.datacite;

import com.fasterxml.jackson.annotation.JsonProperty;

record DcSubject(
    String subject,
    String subjectScheme
) {
  @JsonProperty("schemeUri")
  private static final String SCHEME_URI = null;

  @JsonProperty("valueUri")
  private static final String VALUE_URI = null;

  @JsonProperty("classificationCode")
  private static final String CLASSIFICATION_CODE = null;

}
