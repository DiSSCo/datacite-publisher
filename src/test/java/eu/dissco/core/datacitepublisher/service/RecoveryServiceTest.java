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
import static org.mockito.Mockito.doThrow;

import eu.dissco.core.datacitepublisher.domain.DigitalMediaEvent;
import eu.dissco.core.datacitepublisher.domain.DigitalSpecimenEvent;
import eu.dissco.core.datacitepublisher.domain.EventType;
import eu.dissco.core.datacitepublisher.domain.RecoveryEvent;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteConflictException;
import eu.dissco.core.datacitepublisher.exceptions.DoiResolutionException;
import eu.dissco.core.datacitepublisher.exceptions.InvalidRequestException;
import eu.dissco.core.datacitepublisher.properties.DoiConnectionProperties;
import eu.dissco.core.datacitepublisher.web.DoiClient;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecoveryServiceTest {

  @Mock
  private DoiClient doiClient;
  @Mock
  private DataCitePublisherService dataCitePublisherService;
  @Mock
  private DoiConnectionProperties doiConnectionProperties;

  private RecoveryService recoveryService;

  @BeforeEach
  void init() {
    recoveryService = new RecoveryService(doiClient, dataCitePublisherService, MAPPER,
        doiConnectionProperties);
  }

  @Test
  void testRecoverDoisSpecimen() throws Exception {
    // Given
    given(doiClient.resolveDois(List.of(DOI, DOI_ALT)))
        .willReturn(givenDigitalSpecimenPidRecord());
    given(doiConnectionProperties.getMaxDois()).willReturn(10);

    // When
    recoveryService.recoverDataciteDois(givenRecoveryEvent());

    // Then
    then(dataCitePublisherService).should()
        .handleMessages(new DigitalSpecimenEvent(givenDigitalSpecimen(), EventType.CREATE));
    then(dataCitePublisherService).should()
        .handleMessages(new DigitalSpecimenEvent(givenDigitalSpecimen(PID_ALT), EventType.CREATE));
  }

  @Test
  void testRecoverDoisSpecimenUnknownEventType() throws Exception {
    // Given
    given(doiClient.resolveDois(List.of(DOI, DOI_ALT)))
        .willReturn(givenDigitalSpecimenPidRecord());
    given(doiConnectionProperties.getMaxDois()).willReturn(10);
    var event = new RecoveryEvent(List.of(DOI, DOI_ALT), null);
    var createEvent = new DigitalSpecimenEvent(givenDigitalSpecimen(), EventType.CREATE);
    doThrow(DataCiteConflictException.class).when(dataCitePublisherService)
        .handleMessages(createEvent);

    // When
    recoveryService.recoverDataciteDois(event);

    // Then
    then(dataCitePublisherService).should()
        .handleMessages(new DigitalSpecimenEvent(givenDigitalSpecimen(), EventType.UPDATE));
    then(dataCitePublisherService).should()
        .handleMessages(new DigitalSpecimenEvent(givenDigitalSpecimen(PID_ALT), EventType.CREATE));
  }

  @Test
  void testRecoverDoisMedia() throws Exception {
    // Given
    given(doiClient.resolveDois(List.of(DOI, DOI_ALT)))
        .willReturn(givenDigitalMediaJson());
    given(doiConnectionProperties.getMaxDois()).willReturn(10);

    // When
    recoveryService.recoverDataciteDois(givenRecoveryEvent());

    // Then
    then(dataCitePublisherService).should()
        .handleMessages(new DigitalMediaEvent(givenDigitalMedia(), EventType.CREATE));
    then(dataCitePublisherService).should()
        .handleMessages(new DigitalMediaEvent(givenDigitalMedia(PID_ALT), EventType.CREATE));
  }

  @Test
  void testRecoverDoisMediaUnknownEventType() throws Exception {
    // Given
    given(doiClient.resolveDois(List.of(DOI, DOI_ALT)))
        .willReturn(givenDigitalMediaJson());
    given(doiConnectionProperties.getMaxDois()).willReturn(10);
    var event = new RecoveryEvent(List.of(DOI, DOI_ALT), null);
    var createEvent = new DigitalMediaEvent(givenDigitalMedia(), EventType.CREATE);
    doThrow(DataCiteConflictException.class).when(dataCitePublisherService)
        .handleMessages(createEvent);

    // When
    recoveryService.recoverDataciteDois(event);

    // Then
    then(dataCitePublisherService).should()
        .handleMessages(new DigitalMediaEvent(givenDigitalMedia(), EventType.UPDATE));
    then(dataCitePublisherService).should()
        .handleMessages(new DigitalMediaEvent(givenDigitalMedia(PID_ALT), EventType.CREATE));
  }

  @Test
  void testRecoverDoisSpecimenTwoPages() {
    // Given
    given(doiConnectionProperties.getMaxDois()).willReturn(1);
    var event = new RecoveryEvent(
        List.of(DOI, DOI_ALT), EventType.CREATE
    );

    // When / then
    assertThrows(InvalidRequestException.class, () -> recoveryService.recoverDataciteDois(event));
  }

  @Test
  void testRecoverDoisMissingData() throws Exception {
    // Given
    var doiMessage = MAPPER.readTree("""
        {
          "links":"https://dev.dissco.tech/api/v1/pids/records"
        }
        """);
    given(doiClient.resolveDois(anyList())).willReturn(doiMessage);
    given(doiConnectionProperties.getMaxDois()).willReturn(10);

    // Then
    assertThrows(DoiResolutionException.class,
        () -> recoveryService.recoverDataciteDois(givenRecoveryEvent()));
  }

  @Test
  void testRecoverDoisDataNotArray() throws Exception {
    // Given
    var doiMessage = MAPPER.readTree("""
        {
          "links":"https://dev.dissco.tech/api/v1/pids/records",
          "data": "yep"
        }
        """);
    given(doiClient.resolveDois(anyList())).willReturn(doiMessage);
    given(doiConnectionProperties.getMaxDois()).willReturn(10);

    // Then
    assertThrows(DoiResolutionException.class,
        () -> recoveryService.recoverDataciteDois(givenRecoveryEvent()));
  }

}
