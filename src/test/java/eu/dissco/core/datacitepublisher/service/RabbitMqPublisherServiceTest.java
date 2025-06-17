package eu.dissco.core.datacitepublisher.service;

import static eu.dissco.core.datacitepublisher.TestUtils.MAPPER;
import static eu.dissco.core.datacitepublisher.TestUtils.givenDigitalMediaEvent;
import static eu.dissco.core.datacitepublisher.TestUtils.givenDigitalSpecimenEvent;
import static eu.dissco.core.datacitepublisher.TestUtils.givenTombstoneEvent;
import static org.mockito.BDDMockito.then;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.core.datacitepublisher.properties.RabbitMqProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class RabbitMqPublisherServiceTest {

  @Mock
  private RabbitTemplate rabbitTemplate;
  private RabbitMqProperties properties = new RabbitMqProperties();
  private RabbitMqPublisherService rabbitMqPublisherService;

  @BeforeEach
  void init() {
    rabbitMqPublisherService = new RabbitMqPublisherService(rabbitTemplate,
        properties, MAPPER);
  }

  @Test
  void testDeadLetterEventSpecimenDoi() throws JsonProcessingException {
    // Given

    // When
    rabbitMqPublisherService.deadLetterEventSpecimenDoi(givenDigitalSpecimenEvent());

    // Then
    then(rabbitTemplate).should().convertAndSend(
        properties.getDlqExchangeName(), properties.getSpecimenDoiDlq(),
        MAPPER.writeValueAsString(givenDigitalSpecimenEvent()));
  }

  @Test
  void testDeadLetterEventSpecimenDoiRaw() throws JsonProcessingException {
    // Given

    // When
    rabbitMqPublisherService.deadLetterEventSpecimenDoiRaw(
        MAPPER.writeValueAsString(givenDigitalSpecimenEvent()));

    // Then
    then(rabbitTemplate).should().convertAndSend(
        properties.getDlqExchangeName(), properties.getSpecimenDoiDlq(),
        MAPPER.writeValueAsString(givenDigitalSpecimenEvent()));
  }

  @Test
  void testDeadLetterEventMediaDoi() throws JsonProcessingException {
    // Given

    // When
    rabbitMqPublisherService.deadLetterEventMediaDoi(givenDigitalMediaEvent());

    // Then
    then(rabbitTemplate).should().convertAndSend(
        properties.getDlqExchangeName(), properties.getMediaDoiDlq(),
        MAPPER.writeValueAsString(givenDigitalMediaEvent()));
  }

  @Test
  void testDeadLetterEventMediaDoiRaw() throws JsonProcessingException {
    // Given

    // When
    rabbitMqPublisherService.deadLetterEventMediaDoiRaw(
        MAPPER.writeValueAsString(givenDigitalMediaEvent()));

    // Then
    then(rabbitTemplate).should().convertAndSend(
        properties.getDlqExchangeName(), properties.getMediaDoiDlq(),
        MAPPER.writeValueAsString(givenDigitalMediaEvent()));
  }

  @Test
  void testDeadLetterEventTombstoneDoi() throws JsonProcessingException {
    // Given

    // When
    rabbitMqPublisherService.deadLetterEventTombstone(givenTombstoneEvent());

    // Then
    then(rabbitTemplate).should().convertAndSend(
        properties.getDlqExchangeName(), properties.getTombstoneDoiDlq(),
        MAPPER.writeValueAsString(givenTombstoneEvent()));
  }

  @Test
  void testDeadLetterEventTombstoneDoiRaw() throws JsonProcessingException {
    // Given

    // When
    rabbitMqPublisherService.deadLetterEventTombstoneRaw(
        MAPPER.writeValueAsString(givenTombstoneEvent()));

    // Then
    then(rabbitTemplate).should().convertAndSend(
        properties.getDlqExchangeName(), properties.getTombstoneDoiDlq(),
        MAPPER.writeValueAsString(givenTombstoneEvent()));
  }
}
