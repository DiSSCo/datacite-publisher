package eu.dissco.core.datacitepublisher.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum FdoType {
  @JsonProperty("https://hdl.handle.net/21.T11148/894b1e6cad57e921764e") DIGITAL_SPECIMEN,
  @JsonProperty("https://hdl.handle.net/21.T11148/bbad8c4e101e8af01115") MEDIA_OBJECT;

  public static FdoType fromString(String type) {
    if ("https://hdl.handle.net/21.T11148/894b1e6cad57e921764e".equals(type)
        || "https://doi.org/21.T11148/894b1e6cad57e921764e".equals(type)) {
      return DIGITAL_SPECIMEN;
    }
    if ("https://hdl.handle.net/21.T11148/bbad8c4e101e8af01115".equals(type)
        || "https://doi.org/21.T11148/bbad8c4e101e8af01115".equals(type)) {
      return MEDIA_OBJECT;
    }
    log.error("Invalid DOI type: {}", type);
    throw new IllegalStateException();
  }

  @Override
  public String toString() {
    if (this.equals(DIGITAL_SPECIMEN)) {
      return "https://hdl.handle.net/21.T11148/894b1e6cad57e921764e";
    }
    return "https://hdl.handle.net/21.T11148/bbad8c4e101e8af01115";
  }


}
