package eu.dissco.core.datacitepublisher.service;

import eu.dissco.core.datacitepublisher.Profiles;
import eu.dissco.core.datacitepublisher.component.XmlLocReader;
import eu.dissco.core.datacitepublisher.domain.DigitalMediaEvent;
import eu.dissco.core.datacitepublisher.domain.DigitalSpecimenEvent;
import eu.dissco.core.datacitepublisher.domain.EventType;
import eu.dissco.core.datacitepublisher.domain.TombstoneEvent;
import eu.dissco.core.datacitepublisher.domain.datacite.DcRequest;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import eu.dissco.core.datacitepublisher.properties.DoiProperties;
import eu.dissco.core.datacitepublisher.web.DataCiteComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
@Slf4j
@Profile({Profiles.PUBLISH, Profiles.WEB})
public class DataCitePublisherService extends DataCiteService {

  @Qualifier("datacite")
  private final DataCiteComponent dataCiteClient;

  public DataCitePublisherService(XmlLocReader xmlLocReader,
      JsonMapper mapper,
      DoiProperties properties, DataCiteComponent dataCiteClient) {
    super(xmlLocReader, mapper, properties);
    this.dataCiteClient = dataCiteClient;
  }

  @Override
  public void handleMessages(DigitalSpecimenEvent digitalSpecimenEvent)
      throws DataCiteApiException {
    var dcRequest = buildDcRequest(digitalSpecimenEvent.pidRecord());
    publishToDataCite(dcRequest, digitalSpecimenEvent.eventType());
  }

  @Override
  public void handleMessages(DigitalMediaEvent digitalMediaEvent) throws DataCiteApiException {
    var dcRequest = buildDcRequest(digitalMediaEvent.pidRecord());
    publishToDataCite(dcRequest, digitalMediaEvent.eventType());
  }

  @Override
  public void tombstoneRecord(TombstoneEvent event) throws DataCiteApiException {
    var dcRecord = dataCiteClient.getDataCiteRecord(event.doi());
    var dcRequest = buildDataCiteTombstoneRequest(dcRecord, event);
    publishToDataCite(dcRequest, EventType.TOMBSTONE);
  }

  protected void publishToDataCite(DcRequest request, EventType eventType)
      throws DataCiteApiException {
    var body = mapper.valueToTree(request);
    log.info("Sending {} request to datacite with DOI {}", eventType.name(),
        request.getData().getAttributes().getDoi());
    if (eventType.equals(EventType.CREATE)) {
      dataCiteClient.createNewDataCiteRecord(body, request.getData().getAttributes().getDoi());
    } else {
      dataCiteClient.updateDataCiteRecord(body, request.getData().getAttributes().getDoi());
    }
    log.info("Successfully {}D DOI {} to datacite", eventType.name(),
        request.getData().getAttributes().getDoi());
  }
}
