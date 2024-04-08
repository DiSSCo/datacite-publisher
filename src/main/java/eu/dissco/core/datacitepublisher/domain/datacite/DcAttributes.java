package eu.dissco.core.datacitepublisher.domain.datacite;

import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.ALT_ID_TYPE_DS;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.ALT_ID_TYPE_MO;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.DC_CREATORS;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.DC_EVENT;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.LANDING_PAGE_DS;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.LANDING_PAGE_MO;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.PREFIX;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.PUBLISHER;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.SCHEMA_VERSION;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import eu.dissco.core.datacitepublisher.exceptions.FdoProfileException;
import eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen;
import eu.dissco.core.datacitepublisher.schemas.MediaObject;
import eu.dissco.core.datacitepublisher.utils.XmlLocReader;
import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@JsonInclude(Include.NON_NULL)
class DcAttributes {

  private final String suffix;
  private final String doi;
  private final List<DcCreator> creators; // IssuedForAgent
  private final List<DcTitle> titles; // ReferentName
  private final Integer publicationYear; // From issueDate
  private final List<DcSubject> subjects; // topic origin, topic domain, topic discipline, topic category (last one to do)
  private final List<DcContributor> contributors; // SpecimenHost
  private final List<DcDate> dates; // IssueDate
  private final List<DcAlternateIdentifier> alternateIdentifiers; // primarySpecimenObjectId
  private final DcType types; // "Digital Specimen" or "Media Object". Resource Type General = Other (TBD)
  private final List<DcRelatedIdentifiers> relatedIdentifiers; // tombstone pids; primary specimenObjectid
  private final List<DcDescription> descriptions; // Specimen: Host + materialSampleType, Media: host + linked object type
  private final String url; // human readable landing page
  private final String publisher = PUBLISHER;
  private final String schemaVersion = SCHEMA_VERSION;
  private static final String event = DC_EVENT;
  @Getter(AccessLevel.NONE)
  private final List<String> xmlLocations;
  @Getter(AccessLevel.NONE)
  private static final DateTimeFormatter dt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public DcAttributes(DigitalSpecimen fdoProfile) throws FdoProfileException {
    this.suffix = getSuffix(fdoProfile.getPid());
    this.doi = PREFIX + "/" + this.suffix;
    this.creators = DC_CREATORS;
    this.titles = setTitles(fdoProfile.getReferentName());
    this.publicationYear = setPublicationYear(fdoProfile.getPidRecordIssueDate());
    this.dates = setDates(fdoProfile.getPidRecordIssueDate());
    this.contributors = setContributors(fdoProfile);
    this.xmlLocations = setXmlLocations(fdoProfile.get10320Loc());
    this.url = XmlLocReader.getLandingPageLocation(xmlLocations, LANDING_PAGE_DS);
    this.alternateIdentifiers = setAltIds(ALT_ID_TYPE_DS, fdoProfile.getPrimarySpecimenObjectId());
    this.types = setType("DigitalSpecimen");
    this.relatedIdentifiers = setRelatedIdentifiers();
    this.descriptions = setDescription(fdoProfile);
    this.subjects = setSubjects(fdoProfile);

  }

  public DcAttributes(MediaObject fdoProfile) throws FdoProfileException {
    this.suffix = getSuffix(fdoProfile.getPid());
    this.doi = PREFIX + "/" + this.suffix;
    this.creators = DC_CREATORS;
    this.titles = setTitles(fdoProfile.getReferentName());
    this.publicationYear = setPublicationYear(fdoProfile.getPidRecordIssueDate());
    this.dates = setDates(fdoProfile.getPidRecordIssueDate());
    this.contributors = setContributors(fdoProfile);
    this.xmlLocations = setXmlLocations(fdoProfile.get10320Loc());
    this.url = XmlLocReader.getLandingPageLocation(xmlLocations, LANDING_PAGE_MO);
    this.alternateIdentifiers = setAltIds(ALT_ID_TYPE_MO, fdoProfile.getPrimaryMediaId());
    this.types = setType("Media Object");
    this.relatedIdentifiers = setRelatedIdentifiers();
    this.descriptions = setDescription(fdoProfile);
    this.subjects = setSubjects(fdoProfile);
  }

  private String getSuffix(String pid) {
    // Captures everything before last "/"
    return pid.replaceAll("^(.*/)", "");
  }

  private List<DcTitle> setTitles(String referentName) {
    return List.of(new DcTitle(referentName));
  }

  private Integer setPublicationYear(String pidIssueDate) throws FdoProfileException {
    try {
      return ZonedDateTime.parse(pidIssueDate).getYear();
    } catch (DateTimeException e) {
      log.error("Unable to read pidIssueDate {} for doi {}", pidIssueDate, doi);
      throw new FdoProfileException("pidIssueDate", pidIssueDate, doi);
    }
  }

  private List<DcSubject> setSubjects(DigitalSpecimen digitalSpecimen) {
    var subjectList = new ArrayList<DcSubject>();
    if (digitalSpecimen.getTopicCategory() != null) {
      subjectList.add(
          new DcSubject(digitalSpecimen.getTopicCategory().value(), "topicCategory"
          ));
    }
    if (digitalSpecimen.getTopicDomain() != null) {
      subjectList.add(
          new DcSubject(digitalSpecimen.getTopicDomain().value(), "topicOrigin"));
    }
    if (digitalSpecimen.getTopicCategory() != null) {
      subjectList.add(
          new DcSubject(digitalSpecimen.getTopicCategory().value(), "topicCategory"
          ));
    }
    return subjectList.isEmpty() ? null : subjectList;
  }

  private List<DcSubject> setSubjects(MediaObject mediaObject) {
    var subjectList = new ArrayList<DcSubject>();
    if (mediaObject.getMediaFormat() != null) {
      subjectList.add(new DcSubject(mediaObject.getMediaFormat().value(), "mediaFormat"));
    }
    subjectList.add(
        new DcSubject(mediaObject.getLinkedDigitalObjectType().value(), "linedDigitalObjectType"));
    return subjectList;

  }

  private List<DcDate> setDates(String pidIssueDate) {
    return List.of(new DcDate(ZonedDateTime.parse(pidIssueDate).format(dt)));
  }

  private List<DcContributor> setContributors(DigitalSpecimen digitalSpecimen) {
    var uri = getIdentifierScheme(digitalSpecimen.getSpecimenHost());
    return List.of(new DcContributor(digitalSpecimen.getSpecimenHostName(), List.of(
        new DcNameIdentifiers(uri.getUri(), digitalSpecimen.getSpecimenHost(),
            uri.getSchemeName()))));
  }

  private List<DcContributor> setContributors(MediaObject mediaObject) {
    var uri = getIdentifierScheme(mediaObject.getMediaHost());
    return List.of(new DcContributor(mediaObject.getMediaHostName(), List.of(
        new DcNameIdentifiers(uri.getUri(), mediaObject.getMediaHost(), uri.getSchemeName()))));
  }

  private List<String> setXmlLocations(String loc) throws FdoProfileException {
    return XmlLocReader.getLocationsFromXml(loc, doi);
  }

  private static UriScheme getIdentifierScheme(String identifier) {
    if (identifier.contains("ror")) {
      return UriScheme.ROR;
    }
    if (identifier.contains("hdl")) {
      return UriScheme.HANDLE;
    }
    return UriScheme.QID;
  }

  private List<DcAlternateIdentifier> setAltIds(String idType, String primaryPhysicalObjectId) {
    return List.of(
        new DcAlternateIdentifier(idType, primaryPhysicalObjectId));
  }

  private DcType setType(String resourceType) {
    return new DcType(resourceType);
  }

  private List<DcDescription> setDescription(DigitalSpecimen digitalSpecimen) {
    String descriptionString = "Digital Specimen for the physical specimen hosted at "
        + digitalSpecimen.getSpecimenHostName();
    descriptionString = digitalSpecimen.getMaterialSampleType() == null ? descriptionString
        : descriptionString + " of materialSampleType " + digitalSpecimen.getMaterialSampleType();
    return List.of(new DcDescription(descriptionString));
  }

  private List<DcDescription> setDescription(MediaObject mediaObject) {
    var descriptionString =
        " Media object hosted at " + mediaObject.getMediaHost() + " for an object of type "
            + mediaObject.getLinkedDigitalObjectType();
    return List.of(new DcDescription(descriptionString));
  }

  private List<DcRelatedIdentifiers> setRelatedIdentifiers() {
    List<DcRelatedIdentifiers> relatedIdentifiersList = new ArrayList<>();
    var locs = new ArrayList<>(xmlLocations);
    locs.remove(url);
    for (var location : locs) {
      relatedIdentifiersList.add(new DcRelatedIdentifiers("IsVariantFormOf", location, "URL"));
    }
    return relatedIdentifiersList;
  }

  protected enum UriScheme {
    ROR("https://ror.org", "ROR"),
    HANDLE("https://hdl.handle.net", "Handle"),
    QID(null, "Q Number");

    final String uri;
    final String schemeName;

    private UriScheme(String uri, String schemeName) {
      this.uri = uri;
      this.schemeName = schemeName;
    }

    public String getUri() {
      return uri;
    }

    public String getSchemeName() {
      return schemeName;
    }
  }

}
