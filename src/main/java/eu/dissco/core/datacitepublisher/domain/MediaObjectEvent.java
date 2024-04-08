package eu.dissco.core.datacitepublisher.domain;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = true)
@Value
public class MediaObjectEvent extends DoiEvent {
  List<MediaObjectEvent> fdoProfiles;

}
