package eu.dissco.core.datacitepublisher.domain.datacite;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.DC_EVENT;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.PUBLISHER;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.SCHEMA_VERSION;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@AllArgsConstructor
@Setter(value = AccessLevel.PACKAGE)
@Getter
@JsonInclude(NON_EMPTY)
public class DcAttributes {

  private String suffix;
  private String doi;
  private List<DcCreator> creators; // IssuedForAgent
  private List<DcTitle> titles; // ReferentName
  private Integer publicationYear; // From issueDate
  private List<DcSubject> subjects; // topic origin, topic domain, topic discipline, topic category (last one to do)
  private List<DcContributor> contributors; // SpecimenHost
  private List<DcDate> dates; // IssueDate
  private List<DcAlternateIdentifier> alternateIdentifiers; // primarySpecimenObjectId
  private DcType types; // "Digital Specimen" or "Media Object". Resource Type General = Other (TBD)
  private List<DcRelatedIdentifiers> relatedIdentifiers; // tombstone pids; primary specimenObjectId; primaryMediaId
  private List<DcDescription> descriptions; // Specimen: Host + materialSampleType, Media: host + linked object type
  private String url; // human readable landing page
  private final DcPublisher publisher = PUBLISHER;
  private final String schemaVersion = SCHEMA_VERSION;
  private final String event = DC_EVENT;

}
