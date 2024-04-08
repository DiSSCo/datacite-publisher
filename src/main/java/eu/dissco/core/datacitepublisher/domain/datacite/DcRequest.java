package eu.dissco.core.datacitepublisher.domain.datacite;

import eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen;
import eu.dissco.core.datacitepublisher.schemas.MediaObject;
import lombok.Getter;

@Getter
public class DcRequest {

  private final DcData data;

  public DcRequest(DigitalSpecimen fdoProfile) {
    this.data = new DcData(fdoProfile);
  }

  public DcRequest(MediaObject fdoProfile) {
    this.data = new DcData(fdoProfile);
  }

}
