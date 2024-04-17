package eu.dissco.core.datacitepublisher.domain.datacite;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@AllArgsConstructor
@Setter(value = AccessLevel.PACKAGE)
@Getter
public class DcContributor {

  private String name;
  private List<DcNameIdentifiers> nameIdentifiers;
  private final String nameType = "Organizational";
  private final String contributorType = "HostingInstitution";

}
