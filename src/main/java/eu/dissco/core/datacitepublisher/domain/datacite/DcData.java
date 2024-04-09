package eu.dissco.core.datacitepublisher.domain.datacite;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DcData {

  private final String type = "dois";
  private DcAttributes attributes;

  public DcData withDcAttributes(DcAttributes a){
    this.attributes = a;
    return this;
  }

}
