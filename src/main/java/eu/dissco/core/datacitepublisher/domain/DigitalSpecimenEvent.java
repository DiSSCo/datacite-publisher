package eu.dissco.core.datacitepublisher.domain;

import eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen;
import java.util.List;

public record DigitalSpecimenEvent(List<DigitalSpecimen> pidRecords, EventType eventType) {

}
