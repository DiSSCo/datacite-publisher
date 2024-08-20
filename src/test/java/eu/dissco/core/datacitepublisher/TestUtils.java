package eu.dissco.core.datacitepublisher;

import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.PUBLISHER;
import static eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen.MaterialSampleType.ORGANISM_PART;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eu.dissco.core.datacitepublisher.configuration.InstantDeserializer;
import eu.dissco.core.datacitepublisher.configuration.InstantSerializer;
import eu.dissco.core.datacitepublisher.domain.EventType;
import eu.dissco.core.datacitepublisher.domain.FdoType;
import eu.dissco.core.datacitepublisher.domain.RecoveryEvent;
import eu.dissco.core.datacitepublisher.domain.TombstoneEvent;
import eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants;
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
import eu.dissco.core.datacitepublisher.domain.datacite.RelationType;
import eu.dissco.core.datacitepublisher.domain.datacite.UriScheme;
import eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen;
import eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen.MaterialSampleType;
import eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen.TopicCategory;
import eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen.TopicDiscipline;
import eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen.TopicDomain;
import eu.dissco.core.datacitepublisher.schemas.MediaObject;
import eu.dissco.core.datacitepublisher.schemas.MediaObject.LinkedDigitalObjectType;
import eu.dissco.core.datacitepublisher.schemas.MediaObject.MediaFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TestUtils {

  private TestUtils() {
  }

  public static final String SUFFIX = "QR1-P21-9FW";
  public static final String PREFIX = "10.3535";
  public static final String PID = "https://doi.org/" + PREFIX + "/" + SUFFIX;
  public static final String DOI = PREFIX + "/" + SUFFIX;
  public static final String DOI_ALT = PREFIX + "/2RL-RRS-4BX";
  public static final String PID_ALT = "https://doi.org/" + DOI_ALT;
  public static final String ROR = "https://ror.org/0566bfb96";
  public static final String HOST_NAME = "Naturalis Biodiversity Center";
  public static final String REFERENT_NAME = "New digital object";
  public static final Instant TOMBSTONED = Instant.parse("2024-04-09T09:59:24.00Z");
  public static final String PID_ISSUE_DATE = "2024-03-08'T'11:17:13Z";
  public static final String LOCS = "<locations><location href=\"https://sandbox.dissco.tech/ds/10.3535/QR1-P21-9FW\" id=\"0\" weight=\"1\"/><location href=\"https://sandbox.dissco.tech/api/v1/specimens/10.3535/QR1-P21-9FW\" id=\"1\" weight=\"0\"/></locations>";
  public static final List<String> LOCS_ARR = List.of(
      "https://sandbox.dissco.tech/ds/10.3535/QR1-P21-9FW",
      "https://sandbox.dissco.tech/api/v1/specimens/10.3535/QR1-P21-9FW");
  public static final TopicDiscipline TOPIC_DISCIPLINE = TopicDiscipline.BOTANY;
  public static final TopicCategory TOPIC_CATEGORY = TopicCategory.ALGAE;
  public static final TopicDomain TOPIC_DOMAIN = TopicDomain.LIFE;
  public static final MaterialSampleType MATERIAL_SAMPLE_TYPE = ORGANISM_PART;
  public static final String LOCAL_ID = "PLANT-123";
  public static final ObjectMapper MAPPER;
  public static final XmlMapper XML_MAPPER;

  static {
    var mapper = new ObjectMapper().findAndRegisterModules();
    SimpleModule dateModule = new SimpleModule();
    dateModule.addSerializer(Instant.class, new InstantSerializer());
    dateModule.addDeserializer(Instant.class, new InstantDeserializer());
    mapper.registerModule(dateModule);
    mapper.setSerializationInclusion(Include.NON_NULL);
    MAPPER = mapper;
  }

  static {
    XML_MAPPER = new XmlMapper();
  }

  public static DcAttributes givenSpecimenDataCiteAttributes() {
    return givenSpecimenDataCiteAttributes(DOI);
  }

  public static DcAttributes givenSpecimenDataCiteAttributes(String doi) {
    return DcAttributes.builder()
        .suffix(SUFFIX)
        .doi(doi)
        .creators(List.of(DcCreator.builder()
            .name(HOST_NAME)
            .nameIdentifiers(List.of(givenIdentifier()))
            .build()))
        .titles(List.of(DcTitle.builder()
            .title(REFERENT_NAME).build()))
        .publicationYear(2024)
        .contributors(List.of(DcContributor.builder()
            .name(HOST_NAME)
            .nameIdentifiers(List.of(givenIdentifier()))
            .build()))
        .alternateIdentifiers(List.of(DcAlternateIdentifier.builder()
            .alternateIdentifierType("primarySpecimenObjectId")
            .alternateIdentifier(LOCAL_ID).build()))
        .dates(List.of(givenDcIssueDate()))
        .relatedIdentifiers(List.of(givenDcRelatedIdentifiers()))
        .descriptions(givenSpecimenDescription())
        .types(givenType(DataCiteConstants.TYPE_DS))
        .url("https://sandbox.dissco.tech/ds/10.3535/QR1-P21-9FW")
        .publisher(PUBLISHER)
        .build();
  }

  private static DcDate givenDcIssueDate() {
    return DcDate.builder()
        .dateType("Issued")
        .date("2024-03-08")
        .build();
  }

  private static DcRelatedIdentifiers givenDcRelatedIdentifiers() {
    return DcRelatedIdentifiers.builder()
        .relationType(RelationType.IS_VARIANT_FORM_OF)
        .relatedIdentifier(
            "https://sandbox.dissco.tech/api/v1/specimens/10.3535/QR1-P21-9FW")
        .relatedIdentifierType("URL")
        .build();
  }


  public static DcType givenType(String resourceType) {
    return DcType.builder()
        .resourceType(resourceType)
        .build();
  }

  public static DcAttributes givenSpecimenDataCiteAttributesFull() {
    return DcAttributes.builder()
        .suffix(SUFFIX)
        .doi(DOI)
        .creators(List.of(DcCreator.builder()
            .name(HOST_NAME)
            .nameIdentifiers(List.of(givenIdentifier()))
            .build()))
        .titles(List.of(DcTitle.builder()
            .title(REFERENT_NAME).build()))
        .publicationYear(2024)
        .contributors(List.of(DcContributor.builder()
            .name(HOST_NAME)
            .nameIdentifiers(List.of(givenIdentifier()))
            .build()))
        .alternateIdentifiers(List.of(DcAlternateIdentifier.builder()
            .alternateIdentifierType("primarySpecimenObjectId")
            .alternateIdentifier(LOCAL_ID).build()))
        .dates(List.of(DcDate.builder()
            .date("2024-03-08")
            .dateType("Issued")
            .build()))
        .relatedIdentifiers(List.of(
            DcRelatedIdentifiers.builder()
                .relationType(RelationType.IS_VARIANT_FORM_OF)
                .relatedIdentifier(
                    "https://sandbox.dissco.tech/api/v1/specimens/10.3535/QR1-P21-9FW")
                .relatedIdentifierType("URL")
                .build()))
        .types(givenType(DataCiteConstants.TYPE_DS))
        .url("https://sandbox.dissco.tech/ds/10.3535/QR1-P21-9FW")
        .subjects(List.of(
            DcSubject.builder()
                .subjectScheme("topicDiscipline")
                .subject(TOPIC_DISCIPLINE.value())
                .build(),
            DcSubject.builder()
                .subjectScheme("topicDomain")
                .subject(TOPIC_DOMAIN.value())
                .build(),
            DcSubject.builder()
                .subject(TOPIC_CATEGORY.value())
                .subjectScheme("topicCategory")
                .build())
        )
        .publisher(PUBLISHER)
        .descriptions(givenSpecimenDescriptionFull())
        .build();
  }

  public static JsonNode givenSpecimenJson() {
    return MAPPER.valueToTree(givenSpecimenDataCiteAttributes());
  }

  public static JsonNode givenSpecimenJson(String doi) {
    return MAPPER.valueToTree(givenSpecimenDataCiteAttributes(doi));
  }

  public static JsonNode givenDcRequest(DcAttributes attributes){
    return MAPPER.valueToTree(
        DcRequest.builder()
            .data(DcData.builder()
                .attributes(attributes)
                .build())
            .build());
  }

  public static DcAttributes givenMediaAttributes() {
    return DcAttributes.builder()
        .suffix(SUFFIX)
        .doi(DOI)
        .creators(List.of(DcCreator.builder()
            .name(HOST_NAME)
            .nameIdentifiers(List.of(givenIdentifier()))
            .build()))
        .titles(List.of(DcTitle.builder()
            .title(REFERENT_NAME)
            .build()))
        .publicationYear(2024)
        .contributors(List.of(DcContributor.builder()
            .name(HOST_NAME)
            .nameIdentifiers(List.of(givenIdentifier()))
            .build()))
        .subjects(List.of(
            DcSubject.builder()
                .subjectScheme("linkedDigitalObjectType")
                .subject(LinkedDigitalObjectType.DIGITAL_SPECIMEN.value())
                .build()))
        .alternateIdentifiers(List.of(
            DcAlternateIdentifier.builder()
                .alternateIdentifierType("primaryMediaId")
                .alternateIdentifier(LOCAL_ID).build()))
        .dates(List.of(DcDate.builder()
            .date("2024-03-08")
            .dateType("Issued")
            .build()))
        .relatedIdentifiers(List.of(
            DcRelatedIdentifiers.builder()
                .relationType(RelationType.IS_VARIANT_FORM_OF)
                .relatedIdentifier(
                    "https://sandbox.dissco.tech/api/v1/specimens/10.3535/QR1-P21-9FW")
                .relatedIdentifierType("URL").build()))
        .descriptions(givenMediaDescriptionFull())
        .types(givenType(DataCiteConstants.TYPE_MO))
        .publisher(PUBLISHER)
        .url("https://sandbox.dissco.tech/ds/10.3535/QR1-P21-9FW")
        .build();
  }

  public static DcAttributes givenMediaAttributesFull() {
    return DcAttributes.builder()
        .suffix(SUFFIX)
        .doi(DOI)
        .creators(List.of(DcCreator.builder()
            .name(HOST_NAME)
            .nameIdentifiers(List.of(givenIdentifier()))
            .build()))
        .titles(List.of(DcTitle.builder()
            .title(REFERENT_NAME)
            .build()))
        .publicationYear(2024)
        .contributors(List.of(DcContributor.builder()
            .name(HOST_NAME)
            .nameIdentifiers(List.of(givenIdentifier()))
            .build()))
        .alternateIdentifiers(List.of(
            DcAlternateIdentifier.builder()
                .alternateIdentifierType("primaryMediaId")
                .alternateIdentifier(LOCAL_ID).build()))
        .dates(List.of(DcDate.builder()
            .dateType("Issued")
            .date("2024-03-08")
            .build()))
        .relatedIdentifiers(List.of(
            DcRelatedIdentifiers.builder()
                .relationType(RelationType.IS_VARIANT_FORM_OF)
                .relatedIdentifier(
                    "https://sandbox.dissco.tech/api/v1/specimens/10.3535/QR1-P21-9FW")
                .relatedIdentifierType("URL").build()))
        .descriptions(givenMediaDescriptionFull())
        .types(givenType(DataCiteConstants.TYPE_MO))
        .url("https://sandbox.dissco.tech/ds/10.3535/QR1-P21-9FW")
        .publisher(PUBLISHER)
        .subjects(List.of(
            DcSubject.builder()
                .subjectScheme("mediaFormat")
                .subject(MediaFormat.IMAGE.value())
                .build(),
            DcSubject.builder()
                .subjectScheme("linkedDigitalObjectType")
                .subject(LinkedDigitalObjectType.DIGITAL_SPECIMEN.value())
                .build())
        )
        .build();
  }

  private static List<DcDescription> givenSpecimenDescription() {
    return List.of(
        DcDescription.builder().description(
            "Digital Specimen for the physical specimen hosted at " + HOST_NAME + "."
        ).build()
    );
  }

  private static List<DcDescription> givenSpecimenDescriptionFull() {
    var descriptions = new ArrayList<>(givenSpecimenDescription());
    descriptions.add(DcDescription.builder().description(
        "Material sample type is " + MATERIAL_SAMPLE_TYPE + ".").build());
    return descriptions;
  }

  private static List<DcDescription> givenMediaDescriptionFull() {
    return List.of(
        DcDescription.builder()
            .description("Media object hosted at " + HOST_NAME + ".")
            .build(),
        DcDescription.builder()
            .description(
                "Is media for an object of type " + LinkedDigitalObjectType.DIGITAL_SPECIMEN.value()
                    + ".")
            .build()
    );
  }

  public static DigitalSpecimen givenDigitalSpecimen() {
    return givenDigitalSpecimen(PID);
  }

  public static DigitalSpecimen givenDigitalSpecimen(String doi) {
    return new DigitalSpecimen()
        .with10320Loc(LOCS)
        .withPid(doi)
        .withIssuedForAgent(ROR)
        .withIssuedForAgentName(HOST_NAME)
        .withPidRecordIssueDate(PID_ISSUE_DATE)
        .withPrimarySpecimenObjectId(LOCAL_ID)
        .withSpecimenHost(ROR)
        .withSpecimenHostName(HOST_NAME)
        .withReferentName(REFERENT_NAME);
  }

  public static DigitalSpecimen givenDigitalSpecimenFull() {
    return givenDigitalSpecimen()
        .withTopicDiscipline(TOPIC_DISCIPLINE)
        .withTopicCategory(TOPIC_CATEGORY)
        .withTopicDomain(TOPIC_DOMAIN)
        .withMaterialSampleType(MATERIAL_SAMPLE_TYPE);
  }

  public static MediaObject givenMediaObject() {
    return givenMediaObject(PID);
  }

  public static MediaObject givenMediaObject(String pid) {
    return new MediaObject()
        .with10320Loc(LOCS)
        .withPid(pid)
        .withIssuedForAgent(ROR)
        .withIssuedForAgentName(HOST_NAME)
        .withPidRecordIssueDate(PID_ISSUE_DATE)
        .withPrimaryMediaId(LOCAL_ID)
        .withReferentName(REFERENT_NAME)
        .withMediaHostName(HOST_NAME)
        .withMediaHost(ROR)
        .withLinkedDigitalObjectType(LinkedDigitalObjectType.DIGITAL_SPECIMEN);
  }


  public static MediaObject givenMediaObjectFull() {
    return givenMediaObject()
        .withMediaFormat(MediaFormat.IMAGE);
  }

  public static TombstoneEvent givenTombstoneEvent() {
    return new TombstoneEvent(
        DOI,
        List.of(givenDcRelatedIdentifiersTombstone())
    );
  }

  private static DcRelatedIdentifiers givenDcRelatedIdentifiersTombstone() {
    return DcRelatedIdentifiers.builder()
        .relatedIdentifier(PID_ALT)
        .relatedIdentifierType("DOI")
        .relationType(RelationType.OBSOLETES)
        .build();
  }

  public static DcRequest givenDcRequestTombstone() {
    var description = new ArrayList<>(givenSpecimenDescription());
    description.add(DcDescription.builder()
        .description("This DOI has been tombstoned")
        .build());
    return DcRequest.builder()
        .data(DcData.builder()
            .attributes(DcAttributes.builder()
                .doi(DOI)
                .relatedIdentifiers(
                    List.of(givenDcRelatedIdentifiers(), givenDcRelatedIdentifiersTombstone()))
                .dates(List.of(
                    givenDcIssueDate(),
                    DcDate.builder()
                        .date("2024-04-09")
                        .dateType("Withdrawn")
                        .build()
                ))
                .descriptions(description)
                .build())
            .build())
        .build();
  }


  public static RecoveryEvent givenRecoveryEvent() {
    return new RecoveryEvent(List.of(DOI, DOI_ALT), EventType.CREATE);
  }

  public static JsonNode givenDigitalSpecimenPidRecord() {
    var specimen1 = MAPPER.createObjectNode()
        .put("type", FdoType.DIGITAL_SPECIMEN.toString())
        .set("attributes", MAPPER.valueToTree(givenDigitalSpecimen()));
    var specimen2 = MAPPER.createObjectNode()
        .put("type", FdoType.DIGITAL_SPECIMEN.toString())
        .set("attributes", MAPPER.valueToTree(givenDigitalSpecimen(PID_ALT)));

    return MAPPER.createObjectNode()
        .put("links", "https://dev.dissco.tech/api/v1/pids/records")
        .set("data", MAPPER.createArrayNode()
            .add(specimen1)
            .add(specimen2));
  }

  public static JsonNode givenDigitalSpecimenPidRecordSingle(String pid) {
    var specimen1 = MAPPER.createObjectNode()
        .put("type", FdoType.DIGITAL_SPECIMEN.toString())
        .set("attributes", MAPPER.valueToTree(givenDigitalSpecimen(pid)));

    return MAPPER.createObjectNode()
        .put("links", "https://dev.dissco.tech/api/v1/pids/records")
        .set("data", MAPPER.createArrayNode()
            .add(specimen1));
  }

  public static JsonNode givenMediaObjectJson() {
    var media1 = MAPPER.createObjectNode()
        .put("type", FdoType.MEDIA_OBJECT.toString())
        .set("attributes", MAPPER.valueToTree(givenMediaObject()));
    var media2 = MAPPER.createObjectNode()
        .put("type", FdoType.MEDIA_OBJECT.toString())
        .set("attributes", MAPPER.valueToTree(givenMediaObject(PID_ALT)));

    return MAPPER.createObjectNode()
        .put("links", "https://dev.dissco.tech/api/v1/pids/records")
        .set("data", MAPPER.createArrayNode()
            .add(media1)
            .add(media2));
  }


  public static DcNameIdentifiers givenIdentifier() {
    return DcNameIdentifiers
        .builder()
        .nameIdentifier(ROR)
        .schemeUri(UriScheme.ROR.getUri())
        .nameIdentifierScheme(UriScheme.ROR.getSchemeName())
        .build();
  }


}
