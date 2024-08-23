package eu.dissco.core.datacitepublisher.service;

import static eu.dissco.core.datacitepublisher.TestUtils.DEFAULT_PUBLISHER;
import static eu.dissco.core.datacitepublisher.TestUtils.DOI;
import static eu.dissco.core.datacitepublisher.TestUtils.LOCS;
import static eu.dissco.core.datacitepublisher.TestUtils.LOCS_ARR;
import static eu.dissco.core.datacitepublisher.TestUtils.MAPPER;
import static eu.dissco.core.datacitepublisher.TestUtils.MEDIA_PAGE;
import static eu.dissco.core.datacitepublisher.TestUtils.PID;
import static eu.dissco.core.datacitepublisher.TestUtils.PREFIX;
import static eu.dissco.core.datacitepublisher.TestUtils.SPECIMEN_PAGE;
import static eu.dissco.core.datacitepublisher.TestUtils.SUFFIX;
import static eu.dissco.core.datacitepublisher.TestUtils.TOMBSTONED;
import static eu.dissco.core.datacitepublisher.TestUtils.givenDcRequest;
import static eu.dissco.core.datacitepublisher.TestUtils.givenDcRequestTombstone;
import static eu.dissco.core.datacitepublisher.TestUtils.givenDigitalSpecimen;
import static eu.dissco.core.datacitepublisher.TestUtils.givenDigitalSpecimenFull;
import static eu.dissco.core.datacitepublisher.TestUtils.givenMediaAttributes;
import static eu.dissco.core.datacitepublisher.TestUtils.givenMediaAttributesFull;
import static eu.dissco.core.datacitepublisher.TestUtils.givenMediaObject;
import static eu.dissco.core.datacitepublisher.TestUtils.givenMediaObjectFull;
import static eu.dissco.core.datacitepublisher.TestUtils.givenSpecimenDataCiteAttributes;
import static eu.dissco.core.datacitepublisher.TestUtils.givenSpecimenDataCiteAttributesFull;
import static eu.dissco.core.datacitepublisher.TestUtils.givenTombstoneEvent;
import static eu.dissco.core.datacitepublisher.TestUtils.givenType;
import static eu.dissco.core.datacitepublisher.properties.DoiProperties.MEDIA_TYPE;
import static eu.dissco.core.datacitepublisher.properties.DoiProperties.SPECIMEN_TYPE;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;

import eu.dissco.core.datacitepublisher.component.XmlLocReader;
import eu.dissco.core.datacitepublisher.domain.DigitalSpecimenEvent;
import eu.dissco.core.datacitepublisher.domain.EventType;
import eu.dissco.core.datacitepublisher.domain.MediaObjectEvent;
import eu.dissco.core.datacitepublisher.domain.datacite.DcAttributes;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteMappingException;
import eu.dissco.core.datacitepublisher.properties.DoiProperties;
import eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen;
import eu.dissco.core.datacitepublisher.schemas.MediaObject;
import eu.dissco.core.datacitepublisher.web.DataCiteClient;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;

@ExtendWith(MockitoExtension.class)
class DataCitePublisherServiceTest {

  @Mock
  private XmlLocReader xmlLocReader;
  @Mock
  private DataCiteClient dataCiteClient;
  @Mock
  DoiProperties properties;
  private DataCitePublisherService service;
  private MockedStatic<Instant> mockedInstant;
  private MockedStatic<Clock> mockedClock;

  @BeforeEach
  void setup() {
    service = new DataCitePublisherService(xmlLocReader, MAPPER, dataCiteClient, properties);
    lenient().when(properties.getPrefix()).thenReturn(PREFIX);
    lenient().when(properties.getDefaultPublisher()).thenReturn(DEFAULT_PUBLISHER);
    lenient().when(properties.getLandingPageSpecimen()).thenReturn(SPECIMEN_PAGE);
    lenient().when(properties.getLandingPageMedia()).thenReturn(MEDIA_PAGE);
  }

  @Test
  void testHandleDigitalSpecimenMessage() throws Exception {
    // Given
    var event = new DigitalSpecimenEvent(givenDigitalSpecimen(), EventType.CREATE);
    var expected = givenDcRequest(givenSpecimenDataCiteAttributes());
    given(xmlLocReader.getLocationsFromXml(LOCS)).willReturn(LOCS_ARR);

    // When
    service.handleMessages(event);

    // Then
    then(dataCiteClient).should().sendDoiRequest(expected, HttpMethod.POST, DOI);
  }

  @Test
  void testHandleDigitalSpecimenMessageUpdate() throws Exception {
    // Given
    var event = new DigitalSpecimenEvent(givenDigitalSpecimen(), EventType.UPDATE);
    var expected = givenDcRequest(givenSpecimenDataCiteAttributes());
    given(xmlLocReader.getLocationsFromXml(LOCS)).willReturn(LOCS_ARR);

    // When
    service.handleMessages(event);

    // Then
    then(dataCiteClient).should().sendDoiRequest(expected, HttpMethod.PUT, DOI);
  }

  @Test
  void testHandleDigitalSpecimenApiException() throws Exception {
    // Given
    var event = new DigitalSpecimenEvent(givenDigitalSpecimen(), EventType.CREATE);
    var requestBody = givenDcRequest(givenSpecimenDataCiteAttributes());
    given(xmlLocReader.getLocationsFromXml(LOCS)).willReturn(LOCS_ARR);
    given(dataCiteClient.sendDoiRequest(requestBody, HttpMethod.POST, DOI)).willThrow(
        DataCiteApiException.class);

    // When / Then
    assertThrows(DataCiteApiException.class, () -> service.handleMessages(event));
  }

  @Test
  void testHandleDigitalSpecimenMessageFull() throws Exception {
    // Given
    var event = new DigitalSpecimenEvent(givenDigitalSpecimenFull(), EventType.CREATE);
    var expected = givenDcRequest(givenSpecimenDataCiteAttributesFull());
    given(xmlLocReader.getLocationsFromXml(LOCS)).willReturn(LOCS_ARR);

    // When
    service.handleMessages(event);

    // Then
    then(dataCiteClient).should().sendDoiRequest(expected, HttpMethod.POST, DOI);
  }

  @Test
  void testHandleMediaObjectMessage() throws Exception {
    // Given
    var event = new MediaObjectEvent(givenMediaObject(), EventType.CREATE);
    var expected = givenDcRequest(givenMediaAttributes());
    given(xmlLocReader.getLocationsFromXml(LOCS)).willReturn(LOCS_ARR);

    // When
    service.handleMessages(event);

    // Then
    then(dataCiteClient).should().sendDoiRequest(expected, HttpMethod.POST, DOI);
  }

  @Test
  void testHandleMediaObjectMessageFull() throws Exception {
    // Given
    var event = new MediaObjectEvent(givenMediaObjectFull(), EventType.CREATE);
    var expected = givenDcRequest(givenMediaAttributesFull());
    given(xmlLocReader.getLocationsFromXml(LOCS)).willReturn(LOCS_ARR);

    // When
    service.handleMessages(event);

    // Then
    then(dataCiteClient).should().sendDoiRequest(expected, HttpMethod.POST, DOI);
  }

  @Test
  void testHandleMessageBadDate() throws Exception {
    // Given
    var event = new DigitalSpecimenEvent(
        givenDigitalSpecimen().withPidRecordIssueDate("bad format"), EventType.CREATE);
    given(xmlLocReader.getLocationsFromXml(LOCS)).willReturn(LOCS_ARR);

    // When
    assertThrows(DataCiteMappingException.class, () -> service.handleMessages(event));
  }

  @Test
  void testHandleDigitalSpecimenMessageTestEnv() throws Exception {
    // Given
    var event = new DigitalSpecimenEvent(givenDigitalSpecimen(), EventType.CREATE);
    var expected = givenDcRequest(givenSpecimenDataCiteAttributes(PREFIX + "/" + SUFFIX));
    given(xmlLocReader.getLocationsFromXml(LOCS)).willReturn(LOCS_ARR);

    // When
    service.handleMessages(event);

    // Then
    then(dataCiteClient).should().sendDoiRequest(expected, HttpMethod.POST, DOI);
  }

  @Test
  void testHandleDigitalSpecimenMessageNulls() throws Exception {
    // Given
    var event = new DigitalSpecimenEvent(
        new DigitalSpecimen()
            .withPid(PID),
        EventType.UPDATE
    );
    var expected = givenDcRequest(
        DcAttributes.builder()
            .doi(DOI)
            .suffix(SUFFIX)
            .types(givenType(SPECIMEN_TYPE))
            .publisher(DEFAULT_PUBLISHER)
            .build()
    );

    // When
    service.handleMessages(event);

    // Then
    then(dataCiteClient).should().sendDoiRequest(expected, HttpMethod.PUT, DOI);
  }

  @Test
  void testHandleMediaObjectMessageNulls() throws Exception {
    // Given
    var event = new MediaObjectEvent(
        new MediaObject()
            .withPid(PID),
        EventType.UPDATE
    );
    var attributes = DcAttributes.builder()
        .doi(DOI)
        .suffix(SUFFIX)
        .types(givenType(MEDIA_TYPE))
        .publisher(DEFAULT_PUBLISHER)
        .build();
    var expected = givenDcRequest(attributes);

    // When
    service.handleMessages(event);

    // Then
    then(dataCiteClient).should().sendDoiRequest(expected, HttpMethod.PUT, DOI);
  }

  @Test
  void testTombstoneRecord() throws Exception {
    // Given
    given(dataCiteClient.getDoiRecord(DOI)).willReturn(givenSpecimenDataCiteAttributes());
    var expected = MAPPER.valueToTree(givenDcRequestTombstone());
    initTime();

    // When
    service.tombstoneRecord(givenTombstoneEvent());

    // Then
    then(dataCiteClient).should().sendDoiRequest(expected, HttpMethod.PUT, DOI);
    mockedInstant.close();
    mockedClock.close();
  }

  private void initTime() {
    Clock clock = Clock.fixed(TOMBSTONED, ZoneOffset.UTC);
    Instant instant = Instant.now(clock);
    mockedInstant = mockStatic(Instant.class);
    mockedInstant.when(Instant::now).thenReturn(instant);
    mockedInstant.when(() -> Instant.from(any())).thenReturn(instant);
    mockedInstant.when(() -> Instant.parse(any())).thenReturn(instant);
    mockedClock = mockStatic(Clock.class);
    mockedClock.when(Clock::systemUTC).thenReturn(clock);
  }

}
