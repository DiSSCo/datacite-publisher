package eu.dissco.core.datacitepublisher.domain;

import eu.dissco.core.datacitepublisher.domain.datacite.DcRelatedIdentifiers;
import java.util.List;

public record TombstoneEvent(String handle, List<DcRelatedIdentifiers> tombstonePids) {


}
