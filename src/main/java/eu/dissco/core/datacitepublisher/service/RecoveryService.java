package eu.dissco.core.datacitepublisher.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.core.datacitepublisher.Profiles;
import eu.dissco.core.datacitepublisher.domain.DigitalMediaEvent;
import eu.dissco.core.datacitepublisher.domain.DigitalSpecimenEvent;
import eu.dissco.core.datacitepublisher.domain.EventType;
import eu.dissco.core.datacitepublisher.domain.FdoType;
import eu.dissco.core.datacitepublisher.domain.RecoveryEvent;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteConflictException;
import eu.dissco.core.datacitepublisher.exceptions.HandleResolutionException;
import eu.dissco.core.datacitepublisher.exceptions.InvalidRequestException;
import eu.dissco.core.datacitepublisher.properties.HandleConnectionProperties;
import eu.dissco.core.datacitepublisher.schemas.DigitalMedia;
import eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen;
import eu.dissco.core.datacitepublisher.web.HandleClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile(Profiles.WEB)
public class RecoveryService {

  private final HandleClient handleClient;
  private final DataCitePublisherService dataCitePublisherService;
  @Qualifier("objectMapper")
  private final ObjectMapper mapper;
  private final HandleConnectionProperties handleConnectionProperties;

  public void recoverDataciteDois(RecoveryEvent event)
      throws HandleResolutionException, JsonProcessingException, DataCiteApiException, InvalidRequestException {
    if (event.dois().size() > handleConnectionProperties.getMaxHandles()) {
      log.error("Attempting to recover {} dois, which exceeds maximum permitted",
          event.dois().size());
      throw new InvalidRequestException(
          "Number of dois can not exceed " + handleConnectionProperties.getMaxHandles());
    }
    recoverDois(event.dois(), event.eventType());
  }

  private void recoverDois(List<String> dois, EventType eventType)
      throws HandleResolutionException, DataCiteApiException, JsonProcessingException, InvalidRequestException {
    var handleResolutionResponse = handleClient.resolveHandles(dois);
    if (handleResolutionResponse.get("data") != null && handleResolutionResponse.get("data").isArray()) {
      var dataNodes = handleResolutionResponse.get("data");
      for (var pidRecordJson : dataNodes) {
        var type = FdoType.fromString(pidRecordJson.get("type").asText());
        if (type.equals(FdoType.DIGITAL_SPECIMEN)) {
          recoverDigitalSpecimen(pidRecordJson.get("attributes"), eventType);
        } else if (type.equals(FdoType.MEDIA_OBJECT)) {
          recoverDigitalMedia(pidRecordJson.get("attributes"), eventType);
        } else {
          throw new InvalidRequestException("Can only recover specimen and media DOIs");
        }
      }
    } else {
      log.error("Unexpected response from handle api: {}", handleResolutionResponse);
      throw new HandleResolutionException();
    }
  }

  private void recoverDigitalSpecimen(JsonNode pidRecordAttributes, EventType eventType)
      throws JsonProcessingException, DataCiteApiException {
    var digitalSpecimen = mapper.treeToValue(pidRecordAttributes, DigitalSpecimen.class);
    if (eventType == null) {
      try {
        dataCitePublisherService.handleMessages(
            new DigitalSpecimenEvent(digitalSpecimen, EventType.CREATE));
      } catch (DataCiteConflictException e) {
        log.debug(e.getMessage());
        dataCitePublisherService.handleMessages(
            new DigitalSpecimenEvent(digitalSpecimen, EventType.UPDATE));
      }
    } else {
      dataCitePublisherService.handleMessages(new DigitalSpecimenEvent(digitalSpecimen, eventType));
    }
  }

  private void recoverDigitalMedia(JsonNode pidRecordAttributes, EventType eventType)
      throws DataCiteApiException, JsonProcessingException {
    var mediaObject = mapper.treeToValue(pidRecordAttributes, DigitalMedia.class);
    dataCitePublisherService.handleMessages(new DigitalMediaEvent(mediaObject, eventType));
  }

}
