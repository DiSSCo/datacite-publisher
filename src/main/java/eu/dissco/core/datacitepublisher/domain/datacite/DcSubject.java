package eu.dissco.core.datacitepublisher.domain.datacite;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DcSubject {
  private String subject;
  private String subjectScheme;

  public DcSubject withSubject(String s){
    this.subject = s;
    return this;
  }

  public DcSubject withSubjectScheme(String s){
    this.subjectScheme = s;
    return this;
  }

}
