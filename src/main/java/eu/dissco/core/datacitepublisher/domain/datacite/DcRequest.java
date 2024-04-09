package eu.dissco.core.datacitepublisher.domain.datacite;

import eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen;
import eu.dissco.core.datacitepublisher.schemas.MediaObject;
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
