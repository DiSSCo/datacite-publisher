package eu.dissco.core.datacitepublisher.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.core.datacitepublisher.domain.DigitalMediaEvent;
import eu.dissco.core.datacitepublisher.domain.DigitalSpecimenEvent;
import eu.dissco.core.datacitepublisher.domain.TombstoneEvent;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import eu.dissco.core.datacitepublisher.exceptions.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitMqConsumerService {

  private static final String ERROR_MSG = "Unable to parse {} event from the handle API";
  @Qualifier("objectMapper")
  private final ObjectMapper mapper;
  private final DataCitePublisherService service;
  private final RabbitMqPublisherService rabbitMqPublisherService;

  @RabbitListener(queues = "${rabbitmq.specimen-doi-queue-name:specimen-doi-queue}",
      containerFactory = "consumerBatchContainerFactory")
  public void getSpecimenMessages(@Payload String message)
      throws InvalidRequestException {
    DigitalSpecimenEvent event = null;
    try {
      event = mapper.readValue(message, DigitalSpecimenEvent.class);
      log.info("Received {} specimen message", event.eventType());
      service.handleMessages(event);
      log.info("Successfully processed event for specimen {}", event.pidRecord().getPid());
    } catch (JsonProcessingException e) {
      log.error(ERROR_MSG, "specimen", e);
      log.info("Message: {}", message);
      rabbitMqPublisherService.deadLetterEventSpecimenDoiRaw(message);
      throw new InvalidRequestException();
    } catch (DataCiteApiException e) {
      rabbitMqPublisherService.deadLetterEventSpecimenDoi(event);
    }
  }

  @RabbitListener(queues = "${rabbitmq.media-doi-queue-name:media-doi-queue}",
      containerFactory = "consumerBatchContainerFactory")
  public void getMediaMessages(@Payload String message) throws InvalidRequestException {
    DigitalMediaEvent event = null;
    try {
      event = mapper.readValue(message, DigitalMediaEvent.class);
      log.info("Received {} media message", event.eventType());
      service.handleMessages(event);
      log.info("Successfully processed event for media {}", event.pidRecord().getPid());
    } catch (JsonProcessingException e) {
      log.error(ERROR_MSG, "media", e);
      log.info("Message: {}", message);
      rabbitMqPublisherService.deadLetterEventMediaDoiRaw(message);
      throw new InvalidRequestException();
    } catch (DataCiteApiException e) {
      rabbitMqPublisherService.deadLetterEventMediaDoi(event);
    }
  }

  @RabbitListener(queues = "${rabbitmq.tombstone-doi-queue-name:tombstone-doi-queue}",
      containerFactory = "consumerBatchContainerFactory")
  public void tombstoneDois(@Payload String message)
      throws InvalidRequestException {
    TombstoneEvent event = null;
    try {
      event = mapper.readValue(message, TombstoneEvent.class);
      service.tombstoneRecord(event);
    } catch (JsonProcessingException e) {
      log.error(ERROR_MSG, message, e);
      rabbitMqPublisherService.deadLetterEventTombstoneRaw(message);
      throw new InvalidRequestException();
    } catch (DataCiteApiException e) {
      rabbitMqPublisherService.deadLetterEventTombstone(event);
    }
  }
}
