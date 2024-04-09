package eu.dissco.core.datacitepublisher.domain.datacite;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DcDate {
  private String date;
  private final String dateType = "Issued";

  public DcDate withDate(String s){
    this.date = s;
    return this;
  }

}
