package eu.dissco.core.datacitepublisher.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.core.datacitepublisher.domain.DigitalSpecimenEvent;
import eu.dissco.core.datacitepublisher.service.DataCitePublisherService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class KafkaConsumerService {

  private final ObjectMapper mapper;
  private final DataCitePublisherService service;

  @KafkaListener(topics = "${kafka.consumer.topic}")
  public void getMessages(@Payload String message) {
    DigitalSpecimenEvent event;

    try {
      event = mapper.readValue(message, DigitalSpecimenEvent.class);
    } catch (JsonProcessingException e) {
      log.error("Unable to parse event from the ");
      return;
    }
    service.handleMessage(event);
  }


}
