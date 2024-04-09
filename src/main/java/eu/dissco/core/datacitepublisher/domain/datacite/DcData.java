package eu.dissco.core.datacitepublisher.domain.datacite;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen;
import eu.dissco.core.datacitepublisher.schemas.MediaObject;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DcData {

  @JsonProperty("type")
  private final String TYPE = "dois";
  private DcAttributes attributes;

  public DcData withDcAttributes(DcAttributes a){
    this.attributes = a;
    return this;
  }

}
