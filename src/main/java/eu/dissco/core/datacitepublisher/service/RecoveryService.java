package eu.dissco.core.datacitepublisher.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.core.datacitepublisher.domain.DigitalSpecimenEvent;
import eu.dissco.core.datacitepublisher.domain.EventType;
import eu.dissco.core.datacitepublisher.domain.FdoType;
import eu.dissco.core.datacitepublisher.domain.MediaObjectEvent;
import eu.dissco.core.datacitepublisher.domain.RecoveryEvent;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import eu.dissco.core.datacitepublisher.exceptions.HandleResolutionException;
import eu.dissco.core.datacitepublisher.properties.HandleConnectionProperties;
import eu.dissco.core.datacitepublisher.schemas.DigitalSpecimen;
import eu.dissco.core.datacitepublisher.schemas.MediaObject;
import eu.dissco.core.datacitepublisher.web.HandleClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecoveryService {

  private final HandleClient handleClient;
  private final DataCitePublisherService dataCitePublisherService;
  private final ObjectMapper mapper;
  private final HandleConnectionProperties handleConnectionProperties;

  public void recoverDataciteDois(RecoveryEvent event)
      throws HandleResolutionException, JsonProcessingException, DataCiteApiException {
    int handlesProcessed = 0;
    while (handlesProcessed < event.handles().size()){
      int upperIndex = getUpperIndex(handlesProcessed, event.handles().size());
      var handles = event.handles().subList(handlesProcessed, upperIndex);
      processResolvedHandles(handles, event.eventType());
      handlesProcessed = upperIndex;
    }
  }

  private int getUpperIndex(int handlesProcessed, int totalHandles) {
    if (handlesProcessed + handleConnectionProperties.getMaxHandles() > totalHandles) {
      return totalHandles;
    }
    return handlesProcessed + handleConnectionProperties.getMaxHandles();
  }

  private void processResolvedHandles(List<String> handles, EventType eventType)
      throws HandleResolutionException, DataCiteApiException, JsonProcessingException {

    var handleResolutionResponse = handleClient.resolveHandles(handles);
    if (handleResolutionResponse.get("data") != null && handleResolutionResponse.get("data").isArray()) {
      var dataNodes = handleResolutionResponse.get("data");
      for (var pidRecordJson : dataNodes) {
        var type = FdoType.fromString(pidRecordJson.get("type").asText());
        if (type.equals(FdoType.DIGITAL_SPECIMEN)) {
          recoverDigitalSpecimen(pidRecordJson.get("attributes"), eventType);
        } else {
          recoverMediaObject(pidRecordJson.get("attributes"), eventType);
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
    dataCitePublisherService.handleMessages(new DigitalSpecimenEvent(digitalSpecimen, eventType));
  }

  private void recoverMediaObject(JsonNode pidRecordAttributes, EventType eventType)
      throws DataCiteApiException, JsonProcessingException {
    var mediaObject = mapper.treeToValue(pidRecordAttributes, MediaObject.class);
    dataCitePublisherService.handleMessages(new MediaObjectEvent(mediaObject, eventType));
  }

}
