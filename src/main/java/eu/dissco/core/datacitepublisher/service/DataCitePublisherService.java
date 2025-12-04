package eu.dissco.core.datacitepublisher.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.core.datacitepublisher.Profiles;
import eu.dissco.core.datacitepublisher.component.XmlLocReader;
import eu.dissco.core.datacitepublisher.domain.DigitalMediaEvent;
import eu.dissco.core.datacitepublisher.domain.DigitalSpecimenEvent;
import eu.dissco.core.datacitepublisher.domain.EventType;
import eu.dissco.core.datacitepublisher.domain.TombstoneEvent;
import eu.dissco.core.datacitepublisher.domain.datacite.DcRequest;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import eu.dissco.core.datacitepublisher.properties.DoiProperties;
import eu.dissco.core.datacitepublisher.web.DataCiteClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Profile({Profiles.PUBLISH, Profiles.WEB})
public class DataCitePublisherService extends DataCiteService {

  @Qualifier("datacite")
  private final DataCiteClient dataCiteClient;

  public DataCitePublisherService(XmlLocReader xmlLocReader,
      @Qualifier("objectMapper")
      ObjectMapper mapper,
      DoiProperties properties, DataCiteClient dataCiteClient) {
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
    var dcRecord = dataCiteClient.getDoiRecord(event.doi());
    var dcRequest = buildDataCiteTombstoneRequest(dcRecord, event);
    publishToDataCite(dcRequest, EventType.TOMBSTONE);
  }

  protected void publishToDataCite(DcRequest request, EventType eventType)
      throws DataCiteApiException {
    var body = mapper.valueToTree(request);
    var method = eventType.equals(EventType.CREATE) ? HttpMethod.POST : HttpMethod.PUT;
    log.info("Sending {} request to datacite with DOI {}", eventType.name(),
        request.getData().getAttributes().getDoi());
    var response = dataCiteClient.sendDoiRequest(body, method,
        request.getData().getAttributes().getDoi());
    log.debug("received response from datacite: {}", response);
    log.info("Successfully {}D DOI {} to datacite", eventType.name(),
        request.getData().getAttributes().getDoi());
  }
}
