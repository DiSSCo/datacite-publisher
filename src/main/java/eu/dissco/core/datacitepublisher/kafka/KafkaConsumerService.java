package eu.dissco.core.datacitepublisher.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.core.datacitepublisher.domain.DigitalSpecimenEvent;
import eu.dissco.core.datacitepublisher.domain.MediaObjectEvent;
import eu.dissco.core.datacitepublisher.service.DataCitePublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

  @Qualifier("object")
  private final ObjectMapper mapper;
  private final DataCitePublisherService service;
  private final KafkaPublisherService kafkaPublisherService;

  @KafkaListener(topics = "${kafka.consumer.topic.specimen}")
  public  void getSpecimenMessages(@Payload String message) {
    DigitalSpecimenEvent event;
    try {
      event = mapper.readValue(message, DigitalSpecimenEvent.class);
    } catch (JsonProcessingException e) {
      log.error("Unable to specimen event from the handle API");
      log.debug(message);
      kafkaPublisherService.sendDlq(message);
      return;
    }
    service.handleMessages(event);
  }

  @KafkaListener(topics = "${kafka.consumer.topic.media}")
  public  void getMediaMessages(@Payload String message) {
    MediaObjectEvent event;
    try {
      event = mapper.readValue(message, MediaObjectEvent.class);
    } catch (JsonProcessingException e) {
      log.error("Unable to media event from the Handle API");
      log.debug(message);
      kafkaPublisherService.sendDlq(message);
      return;
    }
    service.handleMessages(event);
  }


}
