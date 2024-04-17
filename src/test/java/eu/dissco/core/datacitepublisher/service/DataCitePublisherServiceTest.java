package eu.dissco.core.datacitepublisher.service;

import static eu.dissco.core.datacitepublisher.TestUtils.LOCS;
import static eu.dissco.core.datacitepublisher.TestUtils.LOCS_ARR;
import static eu.dissco.core.datacitepublisher.TestUtils.MAPPER;
import static eu.dissco.core.datacitepublisher.TestUtils.SUFFIX;
import static eu.dissco.core.datacitepublisher.TestUtils.givenDigitalSpecimen;
import static eu.dissco.core.datacitepublisher.TestUtils.givenDigitalSpecimenFull;
import static eu.dissco.core.datacitepublisher.TestUtils.givenMediaAttributes;
import static eu.dissco.core.datacitepublisher.TestUtils.givenMediaAttributesFull;
import static eu.dissco.core.datacitepublisher.TestUtils.givenMediaObject;
import static eu.dissco.core.datacitepublisher.TestUtils.givenMediaObjectFull;
import static eu.dissco.core.datacitepublisher.TestUtils.givenSpecimenAttributes;
import static eu.dissco.core.datacitepublisher.TestUtils.givenSpecimenAttributesFull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import eu.dissco.core.datacitepublisher.component.XmlLocReader;
import eu.dissco.core.datacitepublisher.domain.DigitalSpecimenEvent;
import eu.dissco.core.datacitepublisher.domain.EventType;
import eu.dissco.core.datacitepublisher.domain.MediaObjectEvent;
import eu.dissco.core.datacitepublisher.domain.datacite.DcData;
import eu.dissco.core.datacitepublisher.domain.datacite.DcRequest;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteMappingException;
import eu.dissco.core.datacitepublisher.web.DataCiteClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;

@ExtendWith(MockitoExtension.class)
class DataCitePublisherServiceTest {

  @Mock
  private XmlLocReader xmlLocReader;
  @Mock
  private DataCiteClient dataCiteClient;
  @Mock
  private Environment environment;
  private DataCitePublisherService service;

  @BeforeEach
  void setup() {
    service = new DataCitePublisherService(xmlLocReader, MAPPER, dataCiteClient, environment);
  }

  @Test
  void testHandleDigitalSpecimenMessage() throws Exception {
    // Given
    var event = new DigitalSpecimenEvent(givenDigitalSpecimen(), EventType.CREATE);
    var expected = MAPPER.valueToTree(
        DcRequest.builder()
            .data(DcData.builder()
                .attributes(givenSpecimenAttributes())
                .build())
            .build());
    given(xmlLocReader.getLocationsFromXml(LOCS)).willReturn(LOCS_ARR);

    // When
    service.handleMessages(event);

    // Then
    then(dataCiteClient).should().sendDoiRequest(expected, HttpMethod.POST);
  }

  @Test
  void testHandleDigitalSpecimenMessageUpdate() throws Exception {
    // Given
    var event = new DigitalSpecimenEvent(givenDigitalSpecimen(), EventType.UPDATE);
    var expected = MAPPER.valueToTree(
        DcRequest.builder()
            .data(DcData.builder()
                .attributes(givenSpecimenAttributes())
                .build())
            .build());
    given(xmlLocReader.getLocationsFromXml(LOCS)).willReturn(LOCS_ARR);

    // When
    service.handleMessages(event);

    // Then
    then(dataCiteClient).should().sendDoiRequest(expected, HttpMethod.PUT);
  }

  @Test
  void testHandleDigitalSpecimenApiException() throws Exception {
    // Given
    var event = new DigitalSpecimenEvent(givenDigitalSpecimen(), EventType.CREATE);
    var requestBody = MAPPER.valueToTree(
        DcRequest.builder()
            .data(DcData.builder()
                .attributes(givenSpecimenAttributes())
                .build())
            .build());
    given(xmlLocReader.getLocationsFromXml(LOCS)).willReturn(LOCS_ARR);
    given(dataCiteClient.sendDoiRequest(requestBody, HttpMethod.POST)).willThrow(
        DataCiteApiException.class);

    // When / Then
    assertThrows(DataCiteApiException.class, () -> service.handleMessages(event));
  }

  @Test
  void testHandleDigitalSpecimenMessageFull() throws Exception {
    // Given
    var event = new DigitalSpecimenEvent(givenDigitalSpecimenFull(), EventType.CREATE);
    var expected = MAPPER.valueToTree(
        DcRequest.builder()
            .data(DcData.builder()
                .attributes(givenSpecimenAttributesFull())
                .build())
            .build());
    given(xmlLocReader.getLocationsFromXml(LOCS)).willReturn(LOCS_ARR);

    // When
    service.handleMessages(event);

    // Then
    then(dataCiteClient).should().sendDoiRequest(expected, HttpMethod.POST);
  }

  @Test
  void testHandleMediaObjectMessage() throws Exception {
    // Given
    var event = new MediaObjectEvent(givenMediaObject(), EventType.CREATE);
    var expected = MAPPER.valueToTree(
        DcRequest.builder()
            .data(DcData.builder()
                .attributes(givenMediaAttributes())
                .build())
            .build());
    given(xmlLocReader.getLocationsFromXml(LOCS)).willReturn(LOCS_ARR);

    // When
    service.handleMessages(event);

    // Then
    then(dataCiteClient).should().sendDoiRequest(expected, HttpMethod.POST);
  }

  @Test
  void testHandleMediaObjectMessageFull() throws Exception {
    // Given
    var event = new MediaObjectEvent(givenMediaObjectFull(), EventType.CREATE);
    var expected = MAPPER.valueToTree(
        DcRequest.builder()
            .data(DcData.builder()
                .attributes(givenMediaAttributesFull())
                .build())
            .build());
    given(xmlLocReader.getLocationsFromXml(LOCS)).willReturn(LOCS_ARR);

    // When
    service.handleMessages(event);

    // Then
    then(dataCiteClient).should().sendDoiRequest(expected, HttpMethod.POST);
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
    given(environment.matchesProfiles(anyString())).willReturn(true);
    var expected = MAPPER.valueToTree(
        DcRequest.builder()
            .data(DcData.builder()
                .attributes(givenSpecimenAttributes("10.82621/" + SUFFIX))
                .build())
            .build());
    given(xmlLocReader.getLocationsFromXml(LOCS)).willReturn(LOCS_ARR);

    // When
    service.handleMessages(event);

    // Then
    then(dataCiteClient).should().sendDoiRequest(expected, HttpMethod.POST);
  }

}
