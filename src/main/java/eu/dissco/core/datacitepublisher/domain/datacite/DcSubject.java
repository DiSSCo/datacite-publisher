package eu.dissco.core.datacitepublisher.domain.datacite;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DcSubject {
  private String subject;
  private String subjectScheme;
  @JsonProperty("schemeUri")
  private final String SCHEME_URI = null;
  @JsonProperty("valueUri")
  private final String VALUE_URI = null;
  @JsonProperty("classificationCode")
  private final String CLASSIFICATION_CODE = null;

  public DcSubject withSubject(String s){
    this.subject = s;
    return this;
  }

  public DcSubject withSubjectScheme(String s){
    this.subjectScheme = s;
    return this;
  }

}
