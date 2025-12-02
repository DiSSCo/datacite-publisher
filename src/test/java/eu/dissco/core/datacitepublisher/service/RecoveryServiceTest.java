package eu.dissco.core.datacitepublisher.service;

import static eu.dissco.core.datacitepublisher.TestUtils.DOI;
import static eu.dissco.core.datacitepublisher.TestUtils.DOI_ALT;
import static eu.dissco.core.datacitepublisher.TestUtils.MAPPER;
import static eu.dissco.core.datacitepublisher.TestUtils.PID_ALT;
import static eu.dissco.core.datacitepublisher.TestUtils.givenDigitalMedia;
import static eu.dissco.core.datacitepublisher.TestUtils.givenDigitalMediaJson;
import static eu.dissco.core.datacitepublisher.TestUtils.givenDigitalSpecimen;
import static eu.dissco.core.datacitepublisher.TestUtils.givenDigitalSpecimenPidRecord;
import static eu.dissco.core.datacitepublisher.TestUtils.givenRecoveryEvent;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import eu.dissco.core.datacitepublisher.domain.DigitalMediaEvent;
import eu.dissco.core.datacitepublisher.domain.DigitalSpecimenEvent;
import eu.dissco.core.datacitepublisher.domain.EventType;
import eu.dissco.core.datacitepublisher.exceptions.HandleResolutionException;
import eu.dissco.core.datacitepublisher.properties.HandleConnectionProperties;
import eu.dissco.core.datacitepublisher.web.HandleClient;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecoveryServiceTest {

  @Mock
  private HandleClient handleClient;
  @Mock
  private DataCitePublisherService dataCitePublisherService;
  @Mock
  private HandleConnectionProperties handleConnectionProperties;

  private RecoveryService recoveryService;

  @BeforeEach
  void init() {
    recoveryService = new RecoveryService(handleClient, dataCitePublisherService, MAPPER, handleConnectionProperties);
  }

  @Test
  void testRecoverDoisSpecimen() throws Exception {
    // Given
    given(handleClient.resolveHandles(List.of(DOI, DOI_ALT)))
        .willReturn(givenDigitalSpecimenPidRecord());
    given(handleConnectionProperties.getMaxHandles()).willReturn(10);

    // When
    recoveryService.recoverDataciteDois(givenRecoveryEvent());

    // Then
    then(dataCitePublisherService).should()
        .handleMessages(new DigitalSpecimenEvent(givenDigitalSpecimen(), EventType.CREATE));
    then(dataCitePublisherService).should()
        .handleMessages(new DigitalSpecimenEvent(givenDigitalSpecimen(PID_ALT), EventType.CREATE));
  }

  @Test
  void testRecoverDoisMedia() throws Exception {
    // Given
    given(handleClient.resolveHandles(List.of(DOI, DOI_ALT)))
        .willReturn(givenDigitalMediaJson());
    given(handleConnectionProperties.getMaxHandles()).willReturn(10);

    // When
    recoveryService.recoverDataciteDois(givenRecoveryEvent());

    // Then
    then(dataCitePublisherService).should()
        .handleMessages(new DigitalMediaEvent(givenDigitalMedia(), EventType.CREATE));
    then(dataCitePublisherService).should()
        .handleMessages(new DigitalMediaEvent(givenDigitalMedia(PID_ALT), EventType.CREATE));
  }

  @Test
  void testRecoverDoisMissingData() throws Exception {
    // Given
    var handleMessage = MAPPER.readTree("""
        {
          "links":"https://dev.dissco.tech/api/v1/pids/records"
        }
        """);
    given(handleClient.resolveHandles(anyList())).willReturn(handleMessage);
    given(handleConnectionProperties.getMaxHandles()).willReturn(10);

    // Then
    assertThrows(HandleResolutionException.class, () -> recoveryService.recoverDataciteDois(givenRecoveryEvent()));
  }

  @Test
  void testRecoverDoisDataNotArray() throws Exception {
    // Given
    var handleMessage = MAPPER.readTree("""
        {
          "links":"https://dev.dissco.tech/api/v1/pids/records",
          "data": "yep"
        }
        """);
    given(handleClient.resolveHandles(anyList())).willReturn(handleMessage);
    given(handleConnectionProperties.getMaxHandles()).willReturn(10);

    // Then
    assertThrows(HandleResolutionException.class, () -> recoveryService.recoverDataciteDois(givenRecoveryEvent()));
  }

}
