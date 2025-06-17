package eu.dissco.core.datacitepublisher.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.core.datacitepublisher.domain.DigitalMediaEvent;
import eu.dissco.core.datacitepublisher.domain.DigitalSpecimenEvent;
import eu.dissco.core.datacitepublisher.domain.TombstoneEvent;
import eu.dissco.core.datacitepublisher.properties.RabbitMqProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitMqPublisherService {

  private final RabbitTemplate rabbitTemplate;
  private final RabbitMqProperties rabbitMqProperties;
  @Qualifier("objectMapper")
  private final ObjectMapper mapper;

  public void deadLetterEventSpecimenDoi(DigitalSpecimenEvent event) {
    try {
      rabbitTemplate.convertAndSend(rabbitMqProperties.getDlqExchangeName(),
          rabbitMqProperties.getSpecimenDoiDlq(), mapper.writeValueAsString(event));
    } catch (JsonProcessingException e) {
      log.error("Unable to DLQ digital specimen doi event", e);
    }
  }

  public void deadLetterEventMediaDoi(DigitalMediaEvent event) {
    try {
      rabbitTemplate.convertAndSend(rabbitMqProperties.getDlqExchangeName(),
          rabbitMqProperties.getMediaDoiDlq(), mapper.writeValueAsString(event));
    } catch (JsonProcessingException e) {
      log.error("Unable to DLQ digital media doi event", e);
    }
  }

  public void deadLetterEventTombstone(TombstoneEvent event) {
    try {
      rabbitTemplate.convertAndSend(rabbitMqProperties.getDlqExchangeName(),
          rabbitMqProperties.getTombstoneDoiDlq(), mapper.writeValueAsString(event));
    } catch (JsonProcessingException e) {
      log.error("Unable to DLQ tombstone event", e);
    }

  }

  public void deadLetterEventSpecimenDoiRaw(String event) {
    rabbitTemplate.convertAndSend(rabbitMqProperties.getDlqExchangeName(),
        rabbitMqProperties.getSpecimenDoiDlq(), event);
  }

  public void deadLetterEventMediaDoiRaw(String event) {
    rabbitTemplate.convertAndSend(rabbitMqProperties.getDlqExchangeName(),
        rabbitMqProperties.getMediaDoiDlq(), event);
  }

  public void deadLetterEventTombstoneRaw(String event) {
    rabbitTemplate.convertAndSend(rabbitMqProperties.getDlqExchangeName(),
        rabbitMqProperties.getTombstoneDoiDlq(), event);
  }


}
