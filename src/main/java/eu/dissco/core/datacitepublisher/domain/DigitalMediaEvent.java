package eu.dissco.core.datacitepublisher.domain;

import eu.dissco.core.datacitepublisher.schemas.DigitalMedia;

public record DigitalMediaEvent(DigitalMedia pidRecord, EventType eventType) {

}
