package eu.dissco.core.datacitepublisher.domain.datacite;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DcDescription {
  private String description;
  @JsonProperty("descriptionType")
  private static final String DESCRIPTION_TYPE = "TechnicalInfo";

  public DcDescription withDescription(String s){
    this.description = s;
    return this;
  }

}
