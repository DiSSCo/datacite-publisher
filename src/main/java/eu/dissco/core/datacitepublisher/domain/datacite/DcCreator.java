package eu.dissco.core.datacitepublisher.domain.datacite;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record DcCreator(
    String name,
    List<DcNameIdentifiers> nameIdentifiers) {

  @JsonProperty("nameType")
  private static final String NAME_TYPE= "Organizational";
}
