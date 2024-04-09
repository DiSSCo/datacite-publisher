package eu.dissco.core.datacitepublisher.domain.datacite;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DcTitle {
  private String title;

  public DcTitle withTitle(String s){
    this.title = s;
    return this;
  }

}
