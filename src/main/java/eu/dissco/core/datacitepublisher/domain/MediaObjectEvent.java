package eu.dissco.core.datacitepublisher.domain;

import eu.dissco.core.datacitepublisher.schemas.MediaObject;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
public class MediaObjectEvent {
  List<MediaObject> fdoProfiles;
}
