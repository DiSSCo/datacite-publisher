package eu.dissco.core.datacitepublisher.domain.datacite;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DcRequest {

  private DcData data;

  public DcRequest withDcData(DcData d){
    this.data = d;
    return this;
  }

}
