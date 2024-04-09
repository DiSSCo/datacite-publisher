package eu.dissco.core.datacitepublisher.domain.datacite;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
class DcContributor {

  private String name;
  private List<DcNameIdentifiers> nameIdentifiers;

  public DcContributor withName(String s){
    this.name = s;
    return this;
  }

  public DcContributor withNameIdentifiers(List<DcNameIdentifiers> l){
    this.nameIdentifiers = l;
    return this;
  }

  @JsonProperty("nameType")
  private static final String NAME_TYPE = "Organizational";

  @JsonProperty("contributorType")
  private static final String CONTRIBUTOR_TYPE = "HostingInstitution";

}
