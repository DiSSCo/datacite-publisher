package eu.dissco.core.datacitepublisher.domain;

import java.util.List;

public record RecoveryEvent(
    List<String> dois,
    EventType eventType
) {

}
