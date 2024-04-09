package eu.dissco.core.datacitepublisher.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.core.datacitepublisher.domain.DigitalSpecimenEvent;
import eu.dissco.core.datacitepublisher.domain.MediaObjectEvent;
import eu.dissco.core.datacitepublisher.domain.datacite.DcRequest;
import eu.dissco.core.datacitepublisher.exceptions.FdoProfileException;
import eu.dissco.core.datacitepublisher.kafka.KafkaPublisherService;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCitePublisherService {
  private final KafkaPublisherService kafkaPublisherService;
  private final ObjectMapper mapper;

  private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd");


  public void handleMessages(DigitalSpecimenEvent digitalSpecimenEvent){


  }

  public void handleMessages(MediaObjectEvent mediaObjectEvent){

  }

    /*
  public DcAttributes(DigitalSpecimen fdoProfile) {
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
    try {
      this.publicationYear = setPublicationYear(fdoProfile.getPidRecordIssueDate());
      this.xmlLocations = setXmlLocations(fdoProfile.get10320Loc());
    } catch (FdoProfileException e) {
      ObjectMapper mapper = new ObjectMapper();
      try {
        throw new FdoProfileException(mapper.writeValueAsString(fdoProfile));
      } catch (JsonProcessingException e1){
        log.error("Critical error: unable to parse message to dlq", e1);
      }

    }
    this.creators = DC_CREATORS;
    this.titles = setTitles(fdoProfile.getReferentName());
    this.dates = setDates(fdoProfile.getPidRecordIssueDate());
    this.contributors = setContributors(fdoProfile);
    this.url = XmlLocReader.getLandingPageLocation(xmlLocations, LANDING_PAGE_MO);
    this.alternateIdentifiers = setAltIds(ALT_ID_TYPE_MO, fdoProfile.getPrimaryMediaId());
    this.types = setType("Media Object");
    this.relatedIdentifiers = setRelatedIdentifiers();
    this.descriptions = setDescription(fdoProfile);
    this.subjects = setSubjects(fdoProfile);
  }

  private String getSuffix(String pid) {
    // Captures everything before last "/"
    return pid.replaceAll("^(.)", "");
  }

  private List<DcTitle> setTitles(String referentName) {
    return List.of(new DcTitle(referentName));
  }

  private Integer setPublicationYear(String pidIssueDate) throws FdoProfileException {
    try {
      return ZonedDateTime.parse(pidIssueDate).getYear();
    } catch (DateTimeException e) {
      log.error("Unable to read pidIssueDate {} for doi {}", pidIssueDate, doi);
      throw new FdoProfileException("pidIssueDate");
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

    String uri;
    String schemeName;

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
  */

}
