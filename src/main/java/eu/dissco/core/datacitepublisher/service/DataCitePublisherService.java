package eu.dissco.core.datacitepublisher.service;

import static eu.dissco.core.datacitepublisher.configuration.ApplicationConfig.DATACITE_FORMATTER;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.ALT_ID_TYPE_DS;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.ALT_ID_TYPE_MO;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.LANDING_PAGE_DS;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.LANDING_PAGE_MO;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.TYPE_DS;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.TYPE_MO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.core.datacitepublisher.domain.DigitalSpecimenEvent;
import eu.dissco.core.datacitepublisher.domain.EventType;
import eu.dissco.core.datacitepublisher.domain.MediaObjectEvent;
import eu.dissco.core.datacitepublisher.domain.datacite.DcAlternateIdentifier;
import eu.dissco.core.datacitepublisher.domain.datacite.DcAttributes;
import eu.dissco.core.datacitepublisher.domain.datacite.DcContributor;
import eu.dissco.core.datacitepublisher.domain.datacite.DcCreator;
import eu.dissco.core.datacitepublisher.domain.datacite.DcData;
import eu.dissco.core.datacitepublisher.domain.datacite.DcDate;
import eu.dissco.core.datacitepublisher.domain.datacite.DcDescription;
import eu.dissco.core.datacitepublisher.domain.datacite.DcNameIdentifiers;
import eu.dissco.core.datacitepublisher.domain.datacite.DcRelatedIdentifiers;
import eu.dissco.core.datacitepublisher.domain.datacite.DcRequest;
import eu.dissco.core.datacitepublisher.domain.datacite.DcSubject;
import eu.dissco.core.datacitepublisher.domain.datacite.DcTitle;
import eu.dissco.core.datacitepublisher.domain.datacite.DcType;
import eu.dissco.core.datacitepublisher.domain.datacite.UriScheme;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteMappingException;
import eu.dissco.core.datacitepublisher.exceptions.InvalidFdoProfileRecievedException;
import eu.dissco.core.datacitepublisher.kafka.KafkaPublisherService;
import eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen;
import eu.dissco.core.datacitepublisher.schemas.MediaObject;
import eu.dissco.core.datacitepublisher.component.XmlLocReader;
import eu.dissco.core.datacitepublisher.web.DataCiteClient;
import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCitePublisherService {

  private final KafkaPublisherService kafkaPublisherService;
  private final XmlLocReader xmlLocReader;
  @Qualifier("object")
  private final ObjectMapper mapper;
  private final DataCiteClient dataCiteClient;

  public void handleMessages(DigitalSpecimenEvent digitalSpecimenEvent) {
    var dcRequests = digitalSpecimenEvent.pidRecords().stream().map(this::buildDcRequest)
        .toList();
    publishToDataCite(dcRequests, digitalSpecimenEvent.eventType());
  }

  public void handleMessages(MediaObjectEvent mediaObjectEvent) {
    var dcRequests = mediaObjectEvent.pidProfiles().stream().map(this::buildDcRequest).toList();
    publishToDataCite(dcRequests, mediaObjectEvent.eventType());
  }

  private void publishToDataCite(List<DcRequest> requests, EventType eventType) {
    int successCount = 0;
    for (var request : requests) {
      var body = mapper.valueToTree(request);
      try {
        var method = eventType.equals(EventType.CREATE) ? HttpMethod.POST : HttpMethod.PUT;
        var response = dataCiteClient.sendDoiRequest(body, method);
        log.debug("received response from datacite: {}", response);
        successCount = successCount + 1;
      } catch (DataCiteApiException e) {
        publishDlq(request, request.getData().getAttributes().getDoi());
      }
    }
    log.info("Successfully published {} dois to datacite out of {} PIDs", successCount,
        requests.size());
  }

  private DcRequest buildDcRequest(DigitalSpecimen digitalSpecimen) {
    return buildDcRequest(
        digitalSpecimen.get10320Loc(),
        LANDING_PAGE_DS,
        ALT_ID_TYPE_DS,
        digitalSpecimen.getPrimarySpecimenObjectId(),
        digitalSpecimen.getSpecimenHostName(),
        digitalSpecimen.getSpecimenHost(),
        digitalSpecimen.getIssuedForAgentName(),
        digitalSpecimen.getIssuedForAgent(),
        digitalSpecimen.getPidRecordIssueDate(),
        digitalSpecimen.getPid(),
        digitalSpecimen.getReferentName(),
        TYPE_DS,
        digitalSpecimen,
        getDescription(digitalSpecimen),
        getSubjects(digitalSpecimen));
  }

  private DcRequest buildDcRequest(MediaObject mediaObject) {
    return buildDcRequest(
        mediaObject.get10320Loc(),
        LANDING_PAGE_MO,
        ALT_ID_TYPE_MO,
        mediaObject.getPrimaryMediaId(),
        mediaObject.getMediaHostName(),
        mediaObject.getMediaHost(),
        mediaObject.getIssuedForAgentName(),
        mediaObject.getIssuedForAgent(),
        mediaObject.getPidRecordIssueDate(),
        mediaObject.getPid(),
        mediaObject.getReferentName(),
        TYPE_MO,
        mediaObject,
        getDescription(mediaObject),
        getSubjects(mediaObject));
  }

  private DcRequest buildDcRequest(String xmlLoc, String landingPage, String altIdType,
      String localId, String hostName,
      String hostId, String issuedForAgentName, String issuedForAgentId, String pidRecordIssueDate,
      String pid, String referentName, String dcType, Object fdoProfile,
      List<DcDescription> descriptions, List<DcSubject> subjects) {
    try {
      var xmlLocs = xmlLocReader.getLocationsFromXml(xmlLoc);
      var url = XmlLocReader.getLandingPageLocation(xmlLocs, landingPage);
      var issueDate = getDate(pidRecordIssueDate);
      return new DcRequest()
          .withDcData(
              new DcData()
                  .withDcAttributes(new DcAttributes()
                      .withAlternateIdentifiers(
                          getAltIds(altIdType, localId))
                      .withContributors(getContributors(hostName, hostId))
                      .withCreators(getCreator(issuedForAgentName, issuedForAgentId))
                      .withDates(getDates(issueDate))
                      .withDescription(descriptions)
                      .withDoi(getDoi(pid))
                      .withPublicationYear(
                          getPublicationYear(issueDate))
                      .withRelatedIdentifiers(getRelatedIdentifiers(xmlLocs, url))
                      .withSubjects(subjects)
                      .withSuffix(getSuffix(pid))
                      .withTitles(setTitles(referentName))
                      .withType(getDcType(dcType))
                      .withUrl(url))
          );
    } catch (InvalidFdoProfileRecievedException e) {
      publishDlq(fdoProfile, pid);
      throw new DataCiteMappingException();
    }
  }

  private void publishDlq(Object message, String pid) {
    String parsedMessage;
    try {
      parsedMessage = mapper.writeValueAsString(message);
    } catch (JsonProcessingException e1) {
      log.error(
          "An error has occurred mapping given FDO profile to Datacite mapping, can not DLQ for fdo profile {}",
          pid, e1);
      kafkaPublisherService.sendDlq(pid);
      return;
    }
    log.error(
        "An error has occurred mapping the given FDO Profile to DataCite Mapping. See profile: {}",
        parsedMessage);
    kafkaPublisherService.sendDlq(parsedMessage);
  }


  private List<DcAlternateIdentifier> getAltIds(String idType, String altId) {
    return List.of(
        new DcAlternateIdentifier()
            .withAlternateIdentifier(altId)
            .withAlternateIdentifierType(idType));
  }

  private List<DcContributor> getContributors(String hostName, String hostId) {
    return List.of(new DcContributor()
        .withName(hostName)
        .withNameIdentifiers(List.of(
            getNameIdentifiers(hostId)
        )));
  }

  private List<DcCreator> getCreator(String issuedForAgentName, String issuedForAgentId) {
    return List.of(new DcCreator()
        .withName(issuedForAgentName)
        .withNameIdentifiers(List.of(getNameIdentifiers(issuedForAgentId))));
  }

  private DcNameIdentifiers getNameIdentifiers(String id) {
    var uriScheme = UriScheme.determineScheme(id);
    return new DcNameIdentifiers()
        .withNameIdentifierScheme(uriScheme.getSchemeName())
        .withSchemeUri(uriScheme.getUri())
        .withNameIdentifier(id);
  }

  private ZonedDateTime getDate(String pidIssueDate) throws InvalidFdoProfileRecievedException {
    try {
      pidIssueDate = pidIssueDate.replace("'", "");
      return ZonedDateTime.parse(pidIssueDate);
    } catch (DateTimeException e) {
      log.error("Unable to parse date {}", pidIssueDate, e);
      throw new InvalidFdoProfileRecievedException();
    }
  }

  private List<DcDate> getDates(ZonedDateTime pidIssueDate) {
    return List.of(new DcDate().withDate(pidIssueDate.format(DATACITE_FORMATTER)));
  }

  private Integer getPublicationYear(ZonedDateTime pidIssueDate) {
    return pidIssueDate.getYear();
  }

  private List<DcDescription> getDescription(DigitalSpecimen digitalSpecimen) {
    String descriptionString = "Digital Specimen for the physical specimen hosted at "
        + digitalSpecimen.getSpecimenHostName();
    descriptionString = digitalSpecimen.getMaterialSampleType() == null ? descriptionString
        : descriptionString + " of materialSampleType " + digitalSpecimen.getMaterialSampleType();
    return List.of(new DcDescription().withDescription(descriptionString));
  }

  private List<DcDescription> getDescription(MediaObject mediaObject) {
    var descriptionString =
        "Media object hosted at " + mediaObject.getMediaHostName() + " for an object of type "
            + mediaObject.getLinkedDigitalObjectType();
    return List.of(new DcDescription().withDescription(descriptionString));
  }

  private String getDoi(String pid) {
    // Captures everything before the second last /
    return pid.replaceAll(".*(?=(?:/[^/]*){2}$)/", "");
  }

  private List<DcSubject> getSubjects(DigitalSpecimen digitalSpecimen) {
    var subjectList = new ArrayList<DcSubject>();
    if (digitalSpecimen.getTopicDiscipline() != null) {
      subjectList.add(
          new DcSubject()
              .withSubjectScheme("topicDiscipline")
              .withSubject(digitalSpecimen.getTopicDiscipline().value()));
    }
    if (digitalSpecimen.getTopicDomain() != null) {
      subjectList.add(
          new DcSubject()
              .withSubjectScheme("topicDomain")
              .withSubject(digitalSpecimen.getTopicDomain().value())
      );
    }
    if (digitalSpecimen.getTopicCategory() != null) {
      subjectList.add(
          new DcSubject()
              .withSubjectScheme("topicCategory")
              .withSubject(digitalSpecimen.getTopicCategory().value())
      );
    }
    return subjectList.isEmpty() ? null : subjectList;
  }

  private List<DcSubject> getSubjects(MediaObject mediaObject) {
    var subjectList = new ArrayList<DcSubject>();
    if (mediaObject.getMediaFormat() != null) {
      subjectList.add(new DcSubject()
          .withSubjectScheme("mediaFormat")
          .withSubject(mediaObject.getMediaFormat()
              .value()));
    }
    subjectList.add(
        new DcSubject()
            .withSubjectScheme("linkedDigitalObjectType")
            .withSubject(mediaObject.getLinkedDigitalObjectType().value())); //
    return subjectList;
  }

  private String getSuffix(String pid) {
    return pid.replaceAll("^(.*/).*/", "");
  }

  private List<DcTitle> setTitles(String referentName) {
    return List.of(new DcTitle().withTitle(referentName));
  }

  private List<DcRelatedIdentifiers> getRelatedIdentifiers(List<String> xmlLocations,
      String landingPage) {
    List<DcRelatedIdentifiers> relatedIdentifiersList = new ArrayList<>();
    var locs = new ArrayList<>(xmlLocations);
    locs.remove(landingPage);
    for (var location : locs) {
      relatedIdentifiersList.add(new DcRelatedIdentifiers()
          .withRelationType("IsVariantFormOf")
          .withRelatedIdentifier(location)
          .withRelatedIdentifierType("URL"));
    }
    return relatedIdentifiersList;
  }

  private DcType getDcType(String type) {
    return new DcType().withDcType(type);
  }

}
