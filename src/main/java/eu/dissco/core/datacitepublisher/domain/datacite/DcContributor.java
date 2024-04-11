package eu.dissco.core.datacitepublisher.domain.datacite;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DcContributor {

  private String name;
  private List<DcNameIdentifiers> nameIdentifiers;

  private final String nameType = "Organizational";
  private final String contributorType = "HostingInstitution";


  public DcContributor withName(String s){
    this.name = s;
    return this;
  }

  public DcContributor withNameIdentifiers(List<DcNameIdentifiers> l){
    this.nameIdentifiers = l;
    return this;
  }

}
