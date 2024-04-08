package eu.dissco.core.datacitepublisher.domain;

import eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = true)
@Value
public class DigitalSpecimenEvent extends DoiEvent {
  List<DigitalSpecimen> fdoProfiles;
}
