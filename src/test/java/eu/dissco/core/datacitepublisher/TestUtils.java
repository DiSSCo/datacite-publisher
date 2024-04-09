package eu.dissco.core.datacitepublisher;

import static eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen.MaterialSampleType.ORGANISM_PART;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import eu.dissco.core.datacitepublisher.configuration.InstantDeserializer;
import eu.dissco.core.datacitepublisher.configuration.InstantSerializer;
import eu.dissco.core.datacitepublisher.domain.datacite.DcAlternateIdentifier;
import eu.dissco.core.datacitepublisher.domain.datacite.DcAttributes;
import eu.dissco.core.datacitepublisher.domain.datacite.DcContributor;
import eu.dissco.core.datacitepublisher.domain.datacite.DcCreator;
import eu.dissco.core.datacitepublisher.domain.datacite.DcDate;
import eu.dissco.core.datacitepublisher.domain.datacite.DcDescription;
import eu.dissco.core.datacitepublisher.domain.datacite.DcNameIdentifiers;
import eu.dissco.core.datacitepublisher.domain.datacite.DcRelatedIdentifiers;
import eu.dissco.core.datacitepublisher.domain.datacite.DcSubject;
import eu.dissco.core.datacitepublisher.domain.datacite.DcTitle;
import eu.dissco.core.datacitepublisher.domain.datacite.DcType;
import eu.dissco.core.datacitepublisher.domain.datacite.UriScheme;
import eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen;
import eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen.MaterialSampleType;
import eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen.TopicDiscipline;
import eu.dissco.core.datacitepublisher.schemas.MediaObject;
import eu.dissco.core.datacitepublisher.schemas.MediaObject.LinkedDigitalObjectType;
import eu.dissco.core.datacitepublisher.schemas.MediaObject.MediaFormat;
import java.time.Instant;
import java.util.List;

public class TestUtils {

  private TestUtils() {
  }

  public static final String PID = "https://doi.org/10.3535/QR1-P21-9FW";
  public static final String SUFFIX = "QR1-P21-9FW";
  public static final String DOI = "10.3535/QR1-P21-9FW";
  public static final String ROR = "https://ror.org/0566bfb96";
  public static final String HOST_NAME = "Naturalis Biodiversity Center";
  public static final String REFERENT_NAME = "New digital object";
  public static final String PID_ISSUE_DATE = "2024-03-08'T'11:17:13Z";
  public static final String LOCS = "<locations><location href=\"https://sandbox.dissco.tech/ds/10.3535/QR1-P21-9FW\" id=\"0\" weight=\"1\"/><location href=\"https://sandbox.dissco.tech/api/v1/specimens/10.3535/QR1-P21-9FW\" id=\"1\" weight=\"0\"/></locations>";
  public static final List<String> LOCS_ARR = List.of(
      "https://sandbox.dissco.tech/ds/10.3535/QR1-P21-9FW",
      "https://sandbox.dissco.tech/api/v1/specimens/10.3535/QR1-P21-9FW");
  public static final TopicDiscipline TOPIC_DISCIPLINE = TopicDiscipline.BOTANY;
  public static final MaterialSampleType MATERIAL_SAMPLE_TYPE = ORGANISM_PART;
  public static final String LOCAL_ID = "PLANT-123";
  public static final ObjectMapper MAPPER;

  static {
    var mapper = new ObjectMapper().findAndRegisterModules();
    SimpleModule dateModule = new SimpleModule();
    dateModule.addSerializer(Instant.class, new InstantSerializer());
    dateModule.addDeserializer(Instant.class, new InstantDeserializer());
    mapper.registerModule(dateModule);
    mapper.setSerializationInclusion(Include.NON_NULL);
    MAPPER = mapper;
  }

  public static DcAttributes givenSpecimenAttributes() {
    return new DcAttributes()
        .withSuffix(SUFFIX)
        .withDoi(DOI)
        .withCreators(List.of(new DcCreator()
            .withName(HOST_NAME)
            .withNameIdentifiers(List.of(givenIdentifier()))))
        .withTitles(List.of(new DcTitle().withTitle(REFERENT_NAME))).withPublicationYear(2024)
        .withContributors(List.of(new DcContributor()
            .withName(HOST_NAME)
            .withNameIdentifiers(List.of(givenIdentifier()))))
        .withSubjects(List.of(new DcSubject()
            .withSubjectScheme("topicDiscipline")
            .withSubject(TOPIC_DISCIPLINE.value())))
        .withAlternateIdentifiers(List.of(new DcAlternateIdentifier()
            .withAlternateIdentifierType("primarySpecimenObjectId")
            .withAlternateIdentifier(LOCAL_ID)))
        .withDates(List.of(new DcDate()
            .withDate("2024-03-08")))
        .withRelatedIdentifiers(List.of(
            new DcRelatedIdentifiers()
                .withRelationType("IsVariantFormOf")
                .withRelatedIdentifier("https://sandbox.dissco.tech/api/v1/specimens/10.3535/QR1-P21-9FW")
                .withRelatedIdentifierType("URL")))
        .withDescription(List.of(new DcDescription()
            .withDescription("Digital Specimen for the physical specimen hosted at " + HOST_NAME
                    + " of materialSampleType " + MATERIAL_SAMPLE_TYPE.value())))
        .withType(new DcType().withDcType("Digital Specimen"))
        .withUrl("https://sandbox.dissco.tech/ds/10.3535/QR1-P21-9FW");
  }

  public static DcAttributes givenMediaAttributes() {
    return new DcAttributes()
        .withSuffix(SUFFIX)
        .withDoi(DOI)
        .withCreators(List.of(
            new DcCreator()
                .withName(HOST_NAME)
                .withNameIdentifiers(List.of(givenIdentifier()))))
        .withTitles(List.of(new DcTitle().withTitle(REFERENT_NAME)))
        .withPublicationYear(2024)
        .withContributors(List.of(
            new DcContributor()
                .withName(HOST_NAME)
                .withNameIdentifiers(List.of(givenIdentifier()))))
        .withSubjects(List.of(
            new DcSubject()
                .withSubjectScheme("mediaFormat")
                .withSubject(MediaFormat.IMAGE.value()),
            new DcSubject()
                .withSubjectScheme("linkedDigitalObjectType")
                .withSubject(LinkedDigitalObjectType.DIGITAL_SPECIMEN.value())))
        .withAlternateIdentifiers(List.of(
            new DcAlternateIdentifier()
                .withAlternateIdentifierType("primaryMediaId")
                .withAlternateIdentifier(LOCAL_ID)))
        .withDates(List.of(
            new DcDate()
              .withDate("2024-03-08")))
        .withRelatedIdentifiers(List.of(
            new DcRelatedIdentifiers()
                .withRelationType("IsVariantFormOf")
                .withRelatedIdentifier("https://sandbox.dissco.tech/api/v1/specimens/10.3535/QR1-P21-9FW")
                .withRelatedIdentifierType("URL")))
        .withDescription(List.of(new DcDescription()
            .withDescription("Media object hosted at " + HOST_NAME + " for an object of type " + LinkedDigitalObjectType.DIGITAL_SPECIMEN.value())))
        .withType(new DcType().withDcType("Media Object"))
        .withUrl("https://sandbox.dissco.tech/ds/10.3535/QR1-P21-9FW");
  }


  public static DigitalSpecimen givenDigitalSpecimen() {
    return new DigitalSpecimen().with10320Loc(LOCS).withPid(PID).withIssuedForAgent(ROR)
        .withIssuedForAgentName(HOST_NAME).withPidRecordIssueDate(PID_ISSUE_DATE)
        .withMaterialSampleType(MATERIAL_SAMPLE_TYPE).withPrimarySpecimenObjectId(LOCAL_ID)
        .withTopicDiscipline(TopicDiscipline.BOTANY).withSpecimenHost(ROR)
        .withSpecimenHostName(HOST_NAME).withReferentName(REFERENT_NAME);
  }

  public static MediaObject givenMediaObject(){
    return new MediaObject()
        .with10320Loc(LOCS)
        .withPid(PID)
        .withIssuedForAgent(ROR)
        .withIssuedForAgentName(HOST_NAME)
        .withPidRecordIssueDate(PID_ISSUE_DATE)
        .withMediaFormat(MediaFormat.IMAGE)
        .withPrimaryMediaId(LOCAL_ID)
        .withReferentName(REFERENT_NAME)
        .withMediaHostName(HOST_NAME)
        .withMediaHost(ROR)
        .withLinkedDigitalObjectType(LinkedDigitalObjectType.DIGITAL_SPECIMEN);
  }


  public static DcNameIdentifiers givenIdentifier() {
    return new DcNameIdentifiers().withNameIdentifier(ROR).withSchemeUri(UriScheme.ROR.getUri())
        .withNameIdentifierScheme(UriScheme.ROR.getSchemeName());
  }


}
