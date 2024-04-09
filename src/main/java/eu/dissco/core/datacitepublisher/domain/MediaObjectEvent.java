package eu.dissco.core.datacitepublisher.domain;

import eu.dissco.core.datacitepublisher.schemas.MediaObject;
import java.util.List;

public record MediaObjectEvent(List<MediaObject> fdoProfiles, EventType eventType) {

}
