package eu.dissco.core.datacitepublisher.service;

import static eu.dissco.core.datacitepublisher.TestUtils.MAPPER;
import static eu.dissco.core.datacitepublisher.TestUtils.givenDigitalMediaEvent;
import static eu.dissco.core.datacitepublisher.TestUtils.givenDigitalSpecimenEvent;
import static eu.dissco.core.datacitepublisher.TestUtils.givenTombstoneEvent;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;

import eu.dissco.core.datacitepublisher.domain.TombstoneEvent;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import eu.dissco.core.datacitepublisher.exceptions.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RabbitMqConsumerServiceTest {

  @Mock
  private DataCitePublisherService dataCiteService;
  @Mock
  private RabbitMqPublisherService rabbitMqPublisherService;
  private RabbitMqConsumerService rabbitMqConsumerService;

  @BeforeEach
  void setup() {
    rabbitMqConsumerService = new RabbitMqConsumerService(MAPPER, dataCiteService,
        rabbitMqPublisherService);
  }

  @Test
  void testHandleSpecimenMessages() throws Exception {
    // Given
    var event = givenDigitalSpecimenEvent();
    var message = MAPPER.writeValueAsString(event);

    // When
    rabbitMqConsumerService.getSpecimenMessages(message);

    // Then
    then(dataCiteService).should().handleMessages(event);
  }

  @Test
  void testHandleSpecimenMessagesBadRequest() throws Exception {
    // Given
    var event = givenDigitalMediaEvent();
    var message = MAPPER.writeValueAsString(event);

    // When / Then
    assertThrows(InvalidRequestException.class,
        () -> rabbitMqConsumerService.getSpecimenMessages(message));
    then(rabbitMqPublisherService).should().deadLetterEventSpecimenDoiRaw(message);
  }

  @Test
  void testHandleSpecimenMessagesDlq() throws Exception {
    // Given
    var event = givenDigitalSpecimenEvent();
    var message = MAPPER.writeValueAsString(event);
    doThrow(DataCiteApiException.class).when(dataCiteService).handleMessages(event);

    // When
    rabbitMqConsumerService.getSpecimenMessages(message);

    // Then
    then(rabbitMqPublisherService).should().deadLetterEventSpecimenDoi(event);
  }

  @Test
  void testHandleMediaMessages() throws Exception {
    // Given
    var event = givenDigitalMediaEvent();
    var message = MAPPER.writeValueAsString(event);

    // When
    rabbitMqConsumerService.getMediaMessages(message);

    // Then
    then(dataCiteService).should().handleMessages(event);
  }

  @Test
  void testHandleMediaMessageBadRequest() throws Exception {
    // Given
    var event = givenDigitalSpecimenEvent();
    var message = MAPPER.writeValueAsString(event);

    // When / Then
    assertThrows(InvalidRequestException.class,
        () -> rabbitMqConsumerService.getMediaMessages(message));
    then(rabbitMqPublisherService).should().deadLetterEventMediaDoiRaw(message);
  }

  @Test
  void testHandleMediaMessagesDlq() throws Exception {
    // Given
    var event = givenDigitalMediaEvent();
    var message = MAPPER.writeValueAsString(event);
    doThrow(DataCiteApiException.class).when(dataCiteService).handleMessages(event);

    // When
    rabbitMqConsumerService.getMediaMessages(message);

    // Then
    then(rabbitMqPublisherService).should().deadLetterEventMediaDoi(event);
  }


  @Test
  void testHandleTombstoneMessages() throws Exception {
    // Given
    var event = givenTombstoneEvent();
    var message = MAPPER.writeValueAsString(event);

    // When
    rabbitMqConsumerService.tombstoneDois(message);

    // Then
    then(dataCiteService).should().tombstoneRecord(any(TombstoneEvent.class));
  }

  @Test
  void testHandleTombstoneMessagesBadRequest() {
    // Given
    var message = "";

    // When / Then
    assertThrows(InvalidRequestException.class,
        () -> rabbitMqConsumerService.tombstoneDois(message));
    then(rabbitMqPublisherService).should().deadLetterEventTombstoneRaw(message);
  }

  @Test
  void testHandleTombstoneMessagesDlq() throws Exception {
    // Given
    var event = givenTombstoneEvent();
    var message = MAPPER.writeValueAsString(event);
    doThrow(DataCiteApiException.class).when(dataCiteService).tombstoneRecord(any());

    // When
    rabbitMqConsumerService.tombstoneDois(message);

    // Then
    then(dataCiteService).should().tombstoneRecord(any(TombstoneEvent.class));
  }

}
