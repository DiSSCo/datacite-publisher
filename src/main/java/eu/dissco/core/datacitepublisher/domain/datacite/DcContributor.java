package eu.dissco.core.datacitepublisher.domain.datacite;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

record DcContributor (
    String name,
    List<DcNameIdentifiers> nameIdentifiers
){
  @JsonProperty("nameType")
  private static final String NAME_TYPE = "Organizational";

  @JsonProperty("contributorType")
  private static final String CONTRIBUTOR_TYPE = "HostingInstitution";

}
