package eu.dissco.core.datacitepublisher.domain.datacite;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
class DcCreator {
  @JsonProperty("nameType")
  private static final String NAME_TYPE= "Organizational";
  private String name;
  private List<DcNameIdentifiers> nameIdentifiers;

  public DcCreator withName(String s){
    this.name = s;
    return this;
  }

  public DcCreator withNameIdentifiers(List<DcNameIdentifiers> l){
    this.nameIdentifiers = l;
    return this;
  }

}
