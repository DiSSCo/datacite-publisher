package eu.dissco.core.datacitepublisher.domain.datacite;

import eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen;
import eu.dissco.core.datacitepublisher.schemas.MediaObject;
import lombok.Getter;

@Getter
class DcData {
  private final String type = "dois";

  private final DcAttributes attributes;

  protected DcData(DigitalSpecimen fdoProfile) {
    this.attributes = new DcAttributes(fdoProfile);
  }

  protected DcData(MediaObject fdoProfile) {
    this.attributes = new DcAttributes(fdoProfile);
  }

}
