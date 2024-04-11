package eu.dissco.core.datacitepublisher.kafka;

import static eu.dissco.core.datacitepublisher.TestUtils.MAPPER;
import static eu.dissco.core.datacitepublisher.TestUtils.givenDigitalSpecimen;
import static eu.dissco.core.datacitepublisher.TestUtils.givenMediaObject;
import static org.mockito.BDDMockito.then;

import eu.dissco.core.datacitepublisher.domain.DigitalSpecimenEvent;
import eu.dissco.core.datacitepublisher.domain.EventType;
import eu.dissco.core.datacitepublisher.domain.MediaObjectEvent;
import eu.dissco.core.datacitepublisher.service.DataCitePublisherService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerServiceTest {

  @Mock
  private DataCitePublisherService dataCiteService;
  @Mock
  private KafkaPublisherService kafkaPublisherService;
  private KafkaConsumerService kafkaConsumerService;

  @BeforeEach
  void setup(){
    kafkaConsumerService = new KafkaConsumerService(MAPPER, dataCiteService, kafkaPublisherService);
  }

  @Test
  void testHandleSpecimenMessages() throws Exception {
    // Given
    var event = new DigitalSpecimenEvent(List.of(givenDigitalSpecimen()), EventType.CREATE);
    var message = MAPPER.writeValueAsString(event);

    // When
    kafkaConsumerService.getSpecimenMessages(message);

    // Then
    then(dataCiteService).should().handleMessages(event);
  }

  @Test
  void testHandleSpecimenMessagesBadRequest() throws Exception {
    // Given
    var event = new MediaObjectEvent(List.of(givenMediaObject()), EventType.CREATE);
    var message = MAPPER.writeValueAsString(event);

    // When
    kafkaConsumerService.getSpecimenMessages(message);

    // Then
    then(kafkaPublisherService).should().sendDlq(message);
  }

  @Test
  void testHandleMediaMessages() throws Exception {
    // Given
    var event = new MediaObjectEvent(List.of(givenMediaObject()), EventType.CREATE);
    var message = MAPPER.writeValueAsString(event);

    // When
    kafkaConsumerService.getMediaMessages(message);

    // Then
    then(dataCiteService).should().handleMessages(event);
  }

  @Test
  void testHandleMediaMessageBadRequest() throws Exception {
    // Given
    var event = new DigitalSpecimenEvent(List.of(givenDigitalSpecimen()), EventType.CREATE);
    var message = MAPPER.writeValueAsString(event);

    // When
    kafkaConsumerService.getMediaMessages(message);

    // Then
    then(kafkaPublisherService).should().sendDlq(message);
  }






}
