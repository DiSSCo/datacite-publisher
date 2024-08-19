package eu.dissco.core.datacitepublisher.kafka;

import static eu.dissco.core.datacitepublisher.TestUtils.MAPPER;
import static eu.dissco.core.datacitepublisher.TestUtils.givenDigitalSpecimen;
import static eu.dissco.core.datacitepublisher.TestUtils.givenMediaObject;
import static eu.dissco.core.datacitepublisher.TestUtils.givenTombstoneEvent;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import eu.dissco.core.datacitepublisher.domain.DigitalSpecimenEvent;
import eu.dissco.core.datacitepublisher.domain.EventType;
import eu.dissco.core.datacitepublisher.domain.MediaObjectEvent;
import eu.dissco.core.datacitepublisher.domain.TombstoneEvent;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import eu.dissco.core.datacitepublisher.exceptions.InvalidRequestException;
import eu.dissco.core.datacitepublisher.service.DataCitePublisherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerServiceTest {

  @Mock
  private DataCitePublisherService dataCiteService;
  private KafkaConsumerService kafkaConsumerService;

  @BeforeEach
  void setup() {
    kafkaConsumerService = new KafkaConsumerService(MAPPER, dataCiteService);
  }

  @Test
  void testHandleSpecimenMessages() throws Exception {
    // Given
    var event = new DigitalSpecimenEvent(givenDigitalSpecimen(), EventType.CREATE);
    var message = MAPPER.writeValueAsString(event);

    // When
    kafkaConsumerService.getSpecimenMessages(message);

    // Then
    then(dataCiteService).should().handleMessages(event);
  }

  @Test
  void testHandleSpecimenMessagesBadRequest() throws Exception {
    // Given
    var event = new MediaObjectEvent(givenMediaObject(), EventType.CREATE);
    var message = MAPPER.writeValueAsString(event);

    // When / Then
    assertThrows(InvalidRequestException.class,
        () -> kafkaConsumerService.getSpecimenMessages(message));
  }

  @Test
  void testHandleMediaMessages() throws Exception {
    // Given
    var event = new MediaObjectEvent(givenMediaObject(), EventType.CREATE);
    var message = MAPPER.writeValueAsString(event);

    // When
    kafkaConsumerService.getMediaMessages(message);

    // Then
    then(dataCiteService).should().handleMessages(event);
  }

  @Test
  void testHandleMediaMessageBadRequest() throws Exception {
    // Given
    var event = new DigitalSpecimenEvent(givenDigitalSpecimen(), EventType.CREATE);
    var message = MAPPER.writeValueAsString(event);

    // When / Then
    assertThrows(InvalidRequestException.class,
        () -> kafkaConsumerService.getMediaMessages(message));
  }

  @Test
  void testHandleTombstoneMessages() throws Exception  {
    // Given
    var event = givenTombstoneEvent();
    var message = MAPPER.writeValueAsString(event);

    // When
    kafkaConsumerService.tombstoneDois(message);

    // Then
    then(dataCiteService).should().tombstoneRecord(any(TombstoneEvent.class));
  }

  @Test
  void testHandleTombstoneMessagesBadRequest() {
    // Given
    var message = "";

    // When / Then
    assertThrows(InvalidRequestException.class,
        () -> kafkaConsumerService.tombstoneDois(message));
  }

  @Test
  void testDlt() throws Exception {
    // Given
    var spyConsumer = spy(kafkaConsumerService);
    var message = new DigitalSpecimenEvent(givenDigitalSpecimen(), EventType.CREATE);
    doThrow(new DataCiteApiException("")).when(dataCiteService).handleMessages(message);

    // When Then
    assertThrows(DataCiteApiException.class,
        () -> spyConsumer.getSpecimenMessages(MAPPER.writeValueAsString(message)));
  }
}
