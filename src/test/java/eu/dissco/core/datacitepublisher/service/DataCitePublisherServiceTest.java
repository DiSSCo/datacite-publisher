package eu.dissco.core.datacitepublisher.service;

import static eu.dissco.core.datacitepublisher.TestUtils.LOCS;
import static eu.dissco.core.datacitepublisher.TestUtils.LOCS_ARR;
import static eu.dissco.core.datacitepublisher.TestUtils.MAPPER;
import static eu.dissco.core.datacitepublisher.TestUtils.givenDigitalSpecimen;
import static eu.dissco.core.datacitepublisher.TestUtils.givenMediaAttributes;
import static eu.dissco.core.datacitepublisher.TestUtils.givenMediaObject;
import static eu.dissco.core.datacitepublisher.TestUtils.givenSpecimenAttributes;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import eu.dissco.core.datacitepublisher.domain.DigitalSpecimenEvent;
import eu.dissco.core.datacitepublisher.domain.EventType;
import eu.dissco.core.datacitepublisher.domain.MediaObjectEvent;
import eu.dissco.core.datacitepublisher.domain.datacite.DcData;
import eu.dissco.core.datacitepublisher.domain.datacite.DcRequest;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteMappingException;
import eu.dissco.core.datacitepublisher.kafka.KafkaPublisherService;
import eu.dissco.core.datacitepublisher.utils.XmlLocReader;
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
