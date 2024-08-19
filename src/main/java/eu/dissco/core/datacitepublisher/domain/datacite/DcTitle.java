package eu.dissco.core.datacitepublisher.domain.datacite;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@Setter(value = AccessLevel.PACKAGE)
@Getter
@NoArgsConstructor
public class DcTitle {
  private String title;
}
