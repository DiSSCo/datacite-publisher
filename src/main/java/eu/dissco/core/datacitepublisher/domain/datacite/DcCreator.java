package eu.dissco.core.datacitepublisher.domain.datacite;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class DcCreator {
  private final String nameType= "Organizational";
  private String name;
  private List<DcNameIdentifiers> nameIdentifiers;
}
