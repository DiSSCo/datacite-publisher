package eu.dissco.core.datacitepublisher.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.core.datacitepublisher.domain.DigitalSpecimenEvent;
import eu.dissco.core.datacitepublisher.domain.DigitalMediaEvent;
import eu.dissco.core.datacitepublisher.domain.TombstoneEvent;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import eu.dissco.core.datacitepublisher.exceptions.InvalidRequestException;
import eu.dissco.core.datacitepublisher.service.DataCitePublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

  @Qualifier("objectMapper")
  private final ObjectMapper mapper;
  private final DataCitePublisherService service;
  private static final String ERROR_MSG = "Unable to parse {} event from the handle API";

  @RetryableTopic(
      attempts = "1",
      dltStrategy = DltStrategy.FAIL_ON_ERROR)
  @KafkaListener(topics = "${kafka.consumer.topic.specimen}")
  public void getSpecimenMessages(@Payload String message) throws DataCiteApiException, InvalidRequestException {
    try {
      var event = mapper.readValue(message, DigitalSpecimenEvent.class);
      log.info("Received {} specimen message", event.eventType());
      service.handleMessages(event);
      log.info("Successfully processed event for specimen {}", event.pidRecord().getPid());
    } catch (JsonProcessingException e) {
      log.error(ERROR_MSG, "specimen", e);
      log.info("Message: {}", message);
      throw new InvalidRequestException();
    }
  }

  @RetryableTopic(
      attempts = "1",
      dltStrategy = DltStrategy.FAIL_ON_ERROR)
  @KafkaListener(topics = "${kafka.consumer.topic.media}")
  public void getMediaMessages(@Payload String message) throws DataCiteApiException, InvalidRequestException {
    try {
      var event = mapper.readValue(message, DigitalMediaEvent.class);
      log.info("Received {} media message", event.eventType());
      service.handleMessages(event);
      log.info("Successfully processed event for media {}", event.pidRecord().getPid());
    } catch (JsonProcessingException e) {
      log.error(ERROR_MSG, "media", e);
      log.info("Message: {}", message);
      throw new InvalidRequestException();
    }
  }

  @RetryableTopic(
      attempts = "1",
      dltStrategy = DltStrategy.FAIL_ON_ERROR)
  @KafkaListener(topics = "${kafka.consumer.topic.tombstone}")
  public void tombstoneDois(@Payload String message) throws DataCiteApiException, InvalidRequestException {
    try {
      var event = mapper.readValue(message, TombstoneEvent.class);
      service.tombstoneRecord(event);
    } catch (JsonProcessingException e){
      log.error(ERROR_MSG + ". Message: {}", message, e);
      throw new InvalidRequestException();
    }
  }

  @DltHandler
  public void dltHandler(String message,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String receivedTopic) {
    log.info("Message {} received in dlt handler at topic {} ", message, receivedTopic);
  }
}
