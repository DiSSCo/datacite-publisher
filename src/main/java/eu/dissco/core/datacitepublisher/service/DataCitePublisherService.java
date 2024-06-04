package eu.dissco.core.datacitepublisher.service;

import static eu.dissco.core.datacitepublisher.configuration.ApplicationConfig.DATACITE_FORMATTER;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.ALT_ID_TYPE_DS;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.ALT_ID_TYPE_MO;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.LANDING_PAGE_DS;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.LANDING_PAGE_MO;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.TYPE_DS;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.TYPE_MO;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.core.datacitepublisher.component.XmlLocReader;
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
import eu.dissco.core.datacitepublisher.properties.DoiProperties;
import eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen;
import eu.dissco.core.datacitepublisher.schemas.MediaObject;
import eu.dissco.core.datacitepublisher.web.DataCiteClient;
import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
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

  private final XmlLocReader xmlLocReader;
  @Qualifier("objectMapper")
  private final ObjectMapper mapper;
  private final DataCiteClient dataCiteClient;
  private final DoiProperties properties;

  public void handleMessages(DigitalSpecimenEvent digitalSpecimenEvent)
      throws DataCiteApiException {
    var dcRequest = buildDcRequest(digitalSpecimenEvent.pidRecord());
    publishToDataCite(dcRequest, digitalSpecimenEvent.eventType());
  }

  public void handleMessages(MediaObjectEvent mediaObjectEvent) throws DataCiteApiException {
    var dcRequest = buildDcRequest(mediaObjectEvent.pidRecord());
    publishToDataCite(dcRequest, mediaObjectEvent.eventType());
  }

  private void publishToDataCite(DcRequest request, EventType eventType)
      throws DataCiteApiException {
    var body = mapper.valueToTree(request);
    var method = eventType.equals(EventType.CREATE) ? HttpMethod.POST : HttpMethod.PUT;
    log.info("Publishing DOI {} to datacite", request.getData().getAttributes().getDoi());
    var response = dataCiteClient.sendDoiRequest(body, method, request.getData().getAttributes().getDoi());
    log.debug("received response from datacite: {}", response);
    log.info("Successfully published DOI {} to datacite",
        request.getData().getAttributes().getDoi());
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
        getDescriptionForSpecimen(digitalSpecimen),
        getSubjectsForSpecimen(digitalSpecimen));
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
        getDescriptionForMedia(mediaObject),
        getSubjectsForMedia(mediaObject));
  }

  private DcRequest buildDcRequest(String xmlLoc, String landingPage, String altIdType,
      String localId, String hostName,
      String hostId, String issuedForAgentName, String issuedForAgentId, String pidRecordIssueDate,
      String pid, String referentName, String dcType,
      List<DcDescription> descriptions, List<DcSubject> subjects) {
    try {
      var xmlLocs = xmlLocReader.getLocationsFromXml(xmlLoc);
      var url =
          xmlLocs.isEmpty() ? null : XmlLocReader.getLandingPageLocation(xmlLocs, landingPage);
      var issueDate = getDate(pidRecordIssueDate);
      return DcRequest.builder()
          .data(
              DcData.builder()
                  .attributes(DcAttributes.builder()
                      .alternateIdentifiers(
                          getAltIds(altIdType, localId))
                      .contributors(getContributors(hostName, hostId))
                      .creators(getCreator(issuedForAgentName, issuedForAgentId))
                      .dates(getDates(issueDate))
                      .descriptions(descriptions)
                      .doi(getDoi(pid))
                      .publicationYear(
                          getPublicationYear(issueDate))
                      .relatedIdentifiers(getRelatedIdentifiers(xmlLocs, url))
                      .subjects(subjects)
                      .suffix(getSuffix(pid))
                      .titles(getTitles(referentName))
                      .types(getDcType(dcType))
                      .url(url)
                      .build())
                  .build()
          ).build();
    } catch (InvalidFdoProfileRecievedException e) {
      throw new DataCiteMappingException();
    }
  }


  private List<DcAlternateIdentifier> getAltIds(String idType, String altId) {
    if (idType == null || altId == null) {
      return Collections.emptyList();
    }
    return List.of(
        DcAlternateIdentifier.builder()
            .alternateIdentifier(altId)
            .alternateIdentifierType(idType).build());
  }

  private List<DcContributor> getContributors(String hostName, String hostId) {
    if (hostName == null || hostId == null) {
      return Collections.emptyList();
    }
    return List.of(DcContributor.builder()
        .name(hostName)
        .nameIdentifiers(getNameIdentifiers(hostId))
        .build());
  }

  private List<DcCreator> getCreator(String issuedForAgentName, String issuedForAgentId) {
    if (issuedForAgentId == null || issuedForAgentName == null) {
      return Collections.emptyList();
    }
    return List.of(DcCreator.builder()
        .name(issuedForAgentName)
        .nameIdentifiers(getNameIdentifiers(issuedForAgentId))
        .build());
  }

  private List<DcNameIdentifiers> getNameIdentifiers(String id) {
    if (id == null) {
      return Collections.emptyList();
    }
    var uriScheme = UriScheme.determineScheme(id);
    return List.of(DcNameIdentifiers.builder()
        .nameIdentifierScheme(uriScheme.getSchemeName())
        .schemeUri(uriScheme.getUri())
        .nameIdentifier(id)
        .build());
  }

  private ZonedDateTime getDate(String pidIssueDate) throws InvalidFdoProfileRecievedException {
    if (pidIssueDate == null) {
      return null;
    }
    try {
      pidIssueDate = pidIssueDate.replace("'", "");
      return ZonedDateTime.parse(pidIssueDate);
    } catch (DateTimeException e) {
      log.error("Unable to parse date {}", pidIssueDate, e);
      throw new InvalidFdoProfileRecievedException();
    }
  }

  private List<DcDate> getDates(ZonedDateTime pidIssueDate) {
    if (pidIssueDate == null) {
      return Collections.emptyList();
    }
    return List.of(DcDate.builder().date(pidIssueDate.format(DATACITE_FORMATTER)).build());
  }

  private Integer getPublicationYear(ZonedDateTime pidIssueDate) {
    if (pidIssueDate == null) {
      return null;
    }
    return pidIssueDate.getYear();
  }

  private List<DcDescription> getDescriptionForSpecimen(DigitalSpecimen digitalSpecimen) {
    var descriptionList = new ArrayList<DcDescription>();
    if (digitalSpecimen.getSpecimenHostName() != null) {
      descriptionList.add(DcDescription.builder().description(
          "Digital Specimen for the physical specimen hosted at "
              + digitalSpecimen.getSpecimenHostName() + "."
      ).build());
    }
    if (digitalSpecimen.getMaterialSampleType() != null) {
      descriptionList.add(DcDescription.builder().description(
          "Material sample type is " + digitalSpecimen.getMaterialSampleType() + "."
      ).build());
    }
    return descriptionList;
  }

  private List<DcDescription> getDescriptionForMedia(MediaObject mediaObject) {
    var descriptionList = new ArrayList<DcDescription>();
    if (mediaObject.getMediaHostName() != null) {
      descriptionList.add(DcDescription.builder()
          .description("Media object hosted at " + mediaObject.getMediaHostName() + ".")
          .build());
    }
    if (mediaObject.getLinkedDigitalObjectType() != null) {
      descriptionList.add(DcDescription.builder()
              .description("Is media for an object of type " + mediaObject.getLinkedDigitalObjectType() + ".")
          .build());
    }
    return descriptionList;
  }

  private String getDoi(String pid) {
    // Replaces everything before the last slash
    return properties.getPrefix() + "/" + pid.replaceAll("^(.*[\\\\/])", "");
  }

  private List<DcSubject> getSubjectsForSpecimen(DigitalSpecimen digitalSpecimen) {
    var subjectList = new ArrayList<DcSubject>();
    if (digitalSpecimen.getTopicDiscipline() != null) {
      subjectList.add(
          DcSubject.builder()
              .subjectScheme("topicDiscipline")
              .subject(digitalSpecimen.getTopicDiscipline().value())
              .build());
    }
    if (digitalSpecimen.getTopicDomain() != null) {
      subjectList.add(
          DcSubject.builder()
              .subjectScheme("topicDomain")
              .subject(digitalSpecimen.getTopicDomain().value())
              .build()
      );
    }
    if (digitalSpecimen.getTopicCategory() != null) {
      subjectList.add(
          DcSubject.builder()
              .subjectScheme("topicCategory")
              .subject(digitalSpecimen.getTopicCategory().value())
              .build()
      );
    }
    return subjectList;
  }

  private List<DcSubject> getSubjectsForMedia(MediaObject mediaObject) {
    var subjectList = new ArrayList<DcSubject>();
    if (mediaObject.getMediaFormat() != null) {
      subjectList.add(DcSubject.builder()
          .subjectScheme("mediaFormat")
          .subject(mediaObject.getMediaFormat().value())
          .build());
    }
    if (mediaObject.getLinkedDigitalObjectType().value() != null) {
      subjectList.add(
          DcSubject.builder()
              .subjectScheme("linkedDigitalObjectType")
              .subject(mediaObject.getLinkedDigitalObjectType().value())
              .build());
    }
    return subjectList;
  }

  private String getSuffix(String pid) {
    return pid.replaceAll("^(.*/).*/", "");
  }

  private List<DcTitle> getTitles(String referentName) {
    if (referentName == null) {
      return Collections.emptyList();
    }
    return List.of(DcTitle.builder()
        .title(referentName)
        .build());
  }

  private List<DcRelatedIdentifiers> getRelatedIdentifiers(List<String> xmlLocations,
      String landingPage) {
    if (xmlLocations == null || xmlLocations.isEmpty()) {
      return Collections.emptyList();
    }
    List<DcRelatedIdentifiers> relatedIdentifiersList = new ArrayList<>();
    var locs = new ArrayList<>(xmlLocations);
    locs.remove(landingPage);
    for (var location : locs) {
      relatedIdentifiersList.add(DcRelatedIdentifiers.builder()
          .relationType("IsVariantFormOf")
          .relatedIdentifier(location)
          .relatedIdentifierType("URL")
          .build());
    }
    return relatedIdentifiersList;
  }

  private DcType getDcType(String type) {
    if (type == null) {
      return null;
    }
    return DcType.builder()
        .resourceType(type)
        .build();
  }
}
