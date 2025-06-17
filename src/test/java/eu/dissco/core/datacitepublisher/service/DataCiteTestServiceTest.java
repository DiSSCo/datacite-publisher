package eu.dissco.core.datacitepublisher.service;

import static eu.dissco.core.datacitepublisher.TestUtils.MAPPER;
import static eu.dissco.core.datacitepublisher.TestUtils.givenDigitalMediaEvent;
import static eu.dissco.core.datacitepublisher.TestUtils.givenDigitalSpecimenEvent;
import static eu.dissco.core.datacitepublisher.TestUtils.givenTombstoneEvent;
import static org.junit.jupiter.api.Assertions.assertAll;

import eu.dissco.core.datacitepublisher.component.XmlLocReader;
import eu.dissco.core.datacitepublisher.properties.DoiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataCiteTestServiceTest {

  @Mock
  DoiProperties properties = new DoiProperties();
  @Mock
  private XmlLocReader xmlLocReader;
  @Mock
  private DataCiteTestService service;

  @BeforeEach
  void setup() {
    service = new DataCiteTestService(xmlLocReader, MAPPER, properties);
  }

  @Test
  void testHandleMessagesSpecimen() {
    // When / Then
    assertAll(() -> service.handleMessages(givenDigitalSpecimenEvent()));
  }

  @Test
  void testHandleMessagesMedia() {
    // When / Then
    assertAll(() -> service.handleMessages(givenDigitalMediaEvent()));
  }

  @Test
  void testHandleMessagesTombstone() {
    // When / Then
    assertAll(() -> service.tombstoneRecord(givenTombstoneEvent()));
  }

}
