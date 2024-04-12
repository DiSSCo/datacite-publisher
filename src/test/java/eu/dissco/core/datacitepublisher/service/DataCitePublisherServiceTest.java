package eu.dissco.core.datacitepublisher.service;

import static eu.dissco.core.datacitepublisher.TestUtils.LOCS;
import static eu.dissco.core.datacitepublisher.TestUtils.LOCS_ARR;
import static eu.dissco.core.datacitepublisher.TestUtils.MAPPER;
import static eu.dissco.core.datacitepublisher.TestUtils.givenDigitalSpecimen;
import static eu.dissco.core.datacitepublisher.TestUtils.givenDigitalSpecimenFull;
import static eu.dissco.core.datacitepublisher.TestUtils.givenMediaAttributes;
import static eu.dissco.core.datacitepublisher.TestUtils.givenMediaAttributesFull;
import static eu.dissco.core.datacitepublisher.TestUtils.givenMediaObject;
import static eu.dissco.core.datacitepublisher.TestUtils.givenMediaObjectFull;
import static eu.dissco.core.datacitepublisher.TestUtils.givenSpecimenAttributes;
import static eu.dissco.core.datacitepublisher.TestUtils.givenSpecimenAttributesFull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static reactor.core.publisher.Mono.when;

import eu.dissco.core.datacitepublisher.domain.DigitalSpecimenEvent;
import eu.dissco.core.datacitepublisher.domain.EventType;
import eu.dissco.core.datacitepublisher.domain.MediaObjectEvent;
import eu.dissco.core.datacitepublisher.domain.datacite.DcData;
import eu.dissco.core.datacitepublisher.domain.datacite.DcRequest;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteMappingException;
import eu.dissco.core.datacitepublisher.kafka.KafkaPublisherService;
import eu.dissco.core.datacitepublisher.component.XmlLocReader;
import eu.dissco.core.datacitepublisher.web.DataCiteClient;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;

@ExtendWith(MockitoExtension.class)
class DataCitePublisherServiceTest {

  @Mock
  private KafkaPublisherService kafkaPublisherService;
  @Mock
  private XmlLocReader xmlLocReader;
  @Mock
  private DataCiteClient dataCiteClient;
  private DataCitePublisherService service;

  @BeforeEach
  void setup() {
    service = new DataCitePublisherService(kafkaPublisherService, xmlLocReader, MAPPER,
        dataCiteClient);
  }

  @Test
  void testHandleDigitalSpecimenMessage() throws Exception {
    // Given
    var event = new DigitalSpecimenEvent(List.of(givenDigitalSpecimen()), EventType.CREATE);
    var expected = MAPPER.valueToTree(
        new DcRequest().withDcData(new DcData().withDcAttributes(givenSpecimenAttributes())));
    given(xmlLocReader.getLocationsFromXml(LOCS)).willReturn(LOCS_ARR);

    // When
    service.handleMessages(event);

    // Then
    then(dataCiteClient).should().sendDoiRequest(expected, HttpMethod.POST);
  }

  @Test
  void testHandleDigitalSpecimenMessageUpdate() throws Exception {
    // Given
    var event = new DigitalSpecimenEvent(List.of(givenDigitalSpecimen()), EventType.UPDATE);
    var expected = MAPPER.valueToTree(
        new DcRequest().withDcData(new DcData().withDcAttributes(givenSpecimenAttributes())));
    given(xmlLocReader.getLocationsFromXml(LOCS)).willReturn(LOCS_ARR);

    // When
    service.handleMessages(event);

    // Then
    then(dataCiteClient).should().sendDoiRequest(expected, HttpMethod.PUT);
  }

  @Test
  void testHandleDigitalSpecimenApiException() throws Exception {
    // Given
    var event = new DigitalSpecimenEvent(List.of(givenDigitalSpecimen()), EventType.CREATE);
    var requestBody = MAPPER.valueToTree(
        new DcRequest().withDcData(new DcData().withDcAttributes(givenSpecimenAttributes())));
    given(xmlLocReader.getLocationsFromXml(LOCS)).willReturn(LOCS_ARR);
    given(dataCiteClient.sendDoiRequest(requestBody, HttpMethod.POST)).willThrow(DataCiteApiException.class);

    // When
    service.handleMessages(event);

    // Then
    then(kafkaPublisherService).should().sendDlq(MAPPER.writeValueAsString(requestBody));
  }

  @Test
  void testHandleDigitalSpecimenOneApiException() throws Exception {
    // Given
    var event = new DigitalSpecimenEvent(List.of(givenDigitalSpecimen(), givenDigitalSpecimen()), EventType.CREATE);
    var requestBody = MAPPER.valueToTree(
        new DcRequest().withDcData(new DcData().withDcAttributes(givenSpecimenAttributes())));
    given(xmlLocReader.getLocationsFromXml(LOCS)).willReturn(LOCS_ARR);
    given(dataCiteClient.sendDoiRequest(requestBody, HttpMethod.POST))
        .willThrow(DataCiteApiException.class)
        .willReturn(MAPPER.createObjectNode());

    // When
    service.handleMessages(event);

    // Then
    then(dataCiteClient).should(times(2)).sendDoiRequest(requestBody, HttpMethod.POST);
    then(kafkaPublisherService).should(times(1)).sendDlq(MAPPER.writeValueAsString(requestBody));
  }

  @Test
  void testHandleDigitalSpecimenMessageFull() throws Exception {
    // Given
    var event = new DigitalSpecimenEvent(List.of(givenDigitalSpecimenFull()), EventType.CREATE);
    var expected = MAPPER.valueToTree(
        new DcRequest().withDcData(new DcData().withDcAttributes(givenSpecimenAttributesFull())));
    given(xmlLocReader.getLocationsFromXml(LOCS)).willReturn(LOCS_ARR);

    // When
    service.handleMessages(event);

    // Then
    then(dataCiteClient).should().sendDoiRequest(expected, HttpMethod.POST);
  }

  @Test
  void testHandleMediaObjectMessage() throws Exception {
    // Given
    var event = new MediaObjectEvent(List.of(givenMediaObject()), EventType.CREATE);
    var expected = MAPPER.valueToTree(
        new DcRequest().withDcData(new DcData().withDcAttributes(givenMediaAttributes())));
    given(xmlLocReader.getLocationsFromXml(LOCS)).willReturn(LOCS_ARR);

    // When
    service.handleMessages(event);

    // Then
    then(dataCiteClient).should().sendDoiRequest(expected, HttpMethod.POST);
  }

  @Test
  void testHandleMediaObjectMessageFull() throws Exception {
    // Given
    var event = new MediaObjectEvent(List.of(givenMediaObjectFull()), EventType.CREATE);
    var expected = MAPPER.valueToTree(
        new DcRequest().withDcData(new DcData().withDcAttributes(givenMediaAttributesFull())));
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
        List.of(givenDigitalSpecimen().withPidRecordIssueDate("bad format")), EventType.CREATE);
    given(xmlLocReader.getLocationsFromXml(LOCS)).willReturn(LOCS_ARR);

    // When
    assertThrows(DataCiteMappingException.class, () -> service.handleMessages(event));

    // Then
    then(kafkaPublisherService).should().sendDlq(any());
  }


}
