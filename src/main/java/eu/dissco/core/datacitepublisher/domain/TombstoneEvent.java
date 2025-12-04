package eu.dissco.core.datacitepublisher.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.dissco.core.datacitepublisher.domain.datacite.DcRelatedIdentifiers;
import java.util.List;

public record TombstoneEvent(
    String doi,
    @JsonProperty("dcRelatedIdentifiers")
    List<DcRelatedIdentifiers> dcRelatedIdentifiersTombstone) {


}
