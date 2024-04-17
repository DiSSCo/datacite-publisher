package eu.dissco.core.datacitepublisher.domain;

import eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen;

public record DigitalSpecimenEvent(DigitalSpecimen pidRecord, EventType eventType) {

}
