package eu.dissco.core.datacitepublisher.service;


import static eu.dissco.core.datacitepublisher.configuration.ApplicationConfig.DATACITE_FORMATTER;
import static eu.dissco.core.datacitepublisher.properties.DoiProperties.MEDIA_ALT_ID_TYPE;
import static eu.dissco.core.datacitepublisher.properties.DoiProperties.MEDIA_TYPE;
import static eu.dissco.core.datacitepublisher.properties.DoiProperties.RESOURCE_TYPE_GENERAL_DATASET;
import static eu.dissco.core.datacitepublisher.properties.DoiProperties.RESOURCE_TYPE_GENERAL_IMAGE;
import static eu.dissco.core.datacitepublisher.properties.DoiProperties.SPECIMEN_ALT_ID_TYPE;
import static eu.dissco.core.datacitepublisher.properties.DoiProperties.SPECIMEN_TYPE;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.core.datacitepublisher.component.XmlLocReader;
import eu.dissco.core.datacitepublisher.domain.DigitalMediaEvent;
import eu.dissco.core.datacitepublisher.domain.DigitalSpecimenEvent;
import eu.dissco.core.datacitepublisher.domain.TombstoneEvent;
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
import eu.dissco.core.datacitepublisher.domain.datacite.DcRights;
import eu.dissco.core.datacitepublisher.domain.datacite.DcSubject;
import eu.dissco.core.datacitepublisher.domain.datacite.DcTitle;
import eu.dissco.core.datacitepublisher.domain.datacite.DcType;
import eu.dissco.core.datacitepublisher.domain.datacite.RelationType;
import eu.dissco.core.datacitepublisher.domain.datacite.UriScheme;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteMappingException;
import eu.dissco.core.datacitepublisher.exceptions.InvalidFdoProfileReceivedException;
import eu.dissco.core.datacitepublisher.properties.DoiProperties;
import eu.dissco.core.datacitepublisher.schemas.DigitalMedia;
import eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

@RequiredArgsConstructor
@Slf4j
public abstract class DataCiteService {

  protected final XmlLocReader xmlLocReader;
  @Qualifier("objectMapper")
  protected final ObjectMapper mapper;
  protected final DoiProperties properties;


  public void handleMessages(DigitalSpecimenEvent digitalSpecimenEvent)
      throws DataCiteApiException {
  }

  public void tombstoneRecord(TombstoneEvent event) throws DataCiteApiException {
  }

  public void handleMessages(DigitalMediaEvent digitalMediaEvent) throws DataCiteApiException {
  }

  protected DcRequest buildDcRequest(DigitalSpecimen digitalSpecimen) {
    return buildDcRequest(
        digitalSpecimen.get10320Loc(),
        properties.getLandingPageSpecimen(),
        SPECIMEN_ALT_ID_TYPE,
        digitalSpecimen.getNormalisedPrimarySpecimenObjectId(),
        digitalSpecimen.getSpecimenHostName(),
        digitalSpecimen.getSpecimenHost(),
        digitalSpecimen.getPidRecordIssueDate(),
        digitalSpecimen.getPid(),
        digitalSpecimen.getReferentName(),
        SPECIMEN_TYPE,
        getDescriptionForSpecimen(digitalSpecimen),
        getSubjectsForSpecimen(digitalSpecimen),
        getRights());
  }

  protected List<DcRights> getRights() {
    return List.of(
        DcRights.builder()
            .rights("CC0 1.0 Universal")
            .rightsUri("https://spdx.org/licenses/CC0-1.0.json")
            .schemeUri("https://spdx.org/licenses/")
            .rightsIdentifier("CC0-1.0")
            .rightsIdentifierScheme("SPDX")
            .lang("en")
            .build());
  }

  protected DcRequest buildDcRequest(DigitalMedia mediaObject) {
    return buildDcRequest(
        mediaObject.get10320Loc(),
        properties.getLandingPageMedia(),
        MEDIA_ALT_ID_TYPE,
        mediaObject.getPrimaryMediaId(),
        mediaObject.getMediaHostName(),
        mediaObject.getMediaHost(),
        mediaObject.getPidRecordIssueDate(),
        mediaObject.getPid(),
        mediaObject.getReferentName(),
        MEDIA_TYPE,
        getDescriptionForMedia(mediaObject),
        getSubjectsForMedia(mediaObject),
        getRights());
  }

  protected DcRequest buildDataCiteTombstoneRequest(DcAttributes dcAttributes,
      TombstoneEvent event) {
    var description = new ArrayList<>(dcAttributes.getDescriptions());
    description.add(DcDescription.builder()
        .description("This DOI has been tombstoned")
        .build());
    var relatedIdentifiers = new ArrayList<>(dcAttributes.getRelatedIdentifiers());
    relatedIdentifiers.addAll(event.dcRelatedIdentifiersTombstone());
    var dates = new ArrayList<>(dcAttributes.getDates());
    dates.add(DcDate.builder()
        .date(DATACITE_FORMATTER.format(Instant.now()))
        .dateType("Withdrawn")
        .build());
    return DcRequest.builder()
        .data(DcData.builder()
            .attributes(DcAttributes.builder()
                .descriptions(description)
                .rightsList(getRights())
                .relatedIdentifiers(relatedIdentifiers)
                .dates(dates)
                .doi(getDoi(event.handle()))
                .build())
            .build())
        .build();
  }

  private DcRequest buildDcRequest(String xmlLoc, String landingPage, String altIdType,
      String localId, String hostName, String hostId, String pidRecordIssueDate, String pid,
      String referentName, String dcType, List<DcDescription> descriptions,
      List<DcSubject> subjects, List<DcRights> rights) {
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
                      .creators(getCreator(hostName, hostId))
                      .dates(getDates(issueDate))
                      .descriptions(descriptions)
                      .doi(getDoi(pid))
                      .publicationYear(
                          getPublicationYear(issueDate))
                      .relatedIdentifiers(getRelatedIdentifiers(xmlLocs, url))
                      .subjects(subjects)
                      .suffix(getSuffix(pid))
                      .rightsList(rights)
                      .titles(getTitles(referentName))
                      .types(getDcType(dcType))
                      .url(url)
                      .publisher(properties.getDefaultPublisher())
                      .build())
                  .build()
          ).build();
    } catch (InvalidFdoProfileReceivedException e) {
      log.error("Unable to parse Fdo Profile", e);
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
    var uriScheme = UriScheme.determineScheme(id);
    return List.of(DcNameIdentifiers.builder()
        .nameIdentifierScheme(uriScheme.getSchemeName())
        .schemeUri(uriScheme.getUri())
        .nameIdentifier(id)
        .build());
  }

  private ZonedDateTime getDate(String pidIssueDate) throws InvalidFdoProfileReceivedException {
    if (pidIssueDate == null) {
      return null;
    }
    try {
      pidIssueDate = pidIssueDate.replace("'", "");
      return ZonedDateTime.parse(pidIssueDate);
    } catch (DateTimeException e) {
      log.error("Unable to parse date {}", pidIssueDate, e);
      throw new InvalidFdoProfileReceivedException();
    }
  }

  private List<DcDate> getDates(ZonedDateTime pidIssueDate) {
    if (pidIssueDate == null) {
      return Collections.emptyList();
    }
    return List.of(DcDate.builder()
        .date(pidIssueDate.format(DATACITE_FORMATTER))
        .dateType("Issued")
        .build());
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

  private List<DcDescription> getDescriptionForMedia(DigitalMedia mediaObject) {
    var descriptionList = new ArrayList<DcDescription>();
    if (mediaObject.getMediaHostName() != null) {
      descriptionList.add(DcDescription.builder()
          .description("Media object hosted at " + mediaObject.getMediaHostName() + ".")
          .build());
    }
    if (mediaObject.getLinkedDigitalObjectType() != null) {
      descriptionList.add(DcDescription.builder()
          .description(
              "Is media for an object of type " + mediaObject.getLinkedDigitalObjectType() + ".")
          .build());
    }
    return descriptionList;
  }

  protected String getDoi(String pid) {
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

  private List<DcSubject> getSubjectsForMedia(DigitalMedia mediaObject) {
    var subjectList = new ArrayList<DcSubject>();
    if (mediaObject.getMimeType() != null) {
      subjectList.add(DcSubject.builder()
          .subjectScheme("mediaFormat")
          .subject(mediaObject.getMimeType())
          .build());
    }
    if (mediaObject.getLinkedDigitalObjectType() != null) {
      subjectList.add(
          DcSubject.builder()
              .subjectScheme("linkedDigitalObjectType")
              .subject(mediaObject.getLinkedDigitalObjectType())
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
          .relationType(RelationType.IS_VARIANT_FORM_OF)
          .relatedIdentifier(location)
          .relatedIdentifierType("URL")
          .build());
    }
    return relatedIdentifiersList;
  }

  private DcType getDcType(String type) {
    var resourceTypeGeneral =
        type.equals(SPECIMEN_TYPE) ? RESOURCE_TYPE_GENERAL_DATASET : RESOURCE_TYPE_GENERAL_IMAGE;
    return DcType.builder()
        .resourceType(type)
        .resourceTypeGeneral(resourceTypeGeneral)
        .build();
  }

}
