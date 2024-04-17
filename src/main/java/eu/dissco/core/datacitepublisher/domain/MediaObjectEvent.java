package eu.dissco.core.datacitepublisher.domain;

import eu.dissco.core.datacitepublisher.schemas.MediaObject;

public record MediaObjectEvent(MediaObject pidRecord, EventType eventType) {

}
