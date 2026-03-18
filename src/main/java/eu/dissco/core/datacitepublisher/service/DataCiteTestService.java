package eu.dissco.core.datacitepublisher.service;

import eu.dissco.core.datacitepublisher.Profiles;
import eu.dissco.core.datacitepublisher.component.XmlLocReader;
import eu.dissco.core.datacitepublisher.domain.DigitalMediaEvent;
import eu.dissco.core.datacitepublisher.domain.DigitalSpecimenEvent;
import eu.dissco.core.datacitepublisher.domain.TombstoneEvent;
import eu.dissco.core.datacitepublisher.domain.datacite.DcAttributes;
import eu.dissco.core.datacitepublisher.properties.DoiProperties;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Profile(Profiles.TEST)
@Service
@Slf4j
public class DataCiteTestService extends DataCiteService {

	public DataCiteTestService(XmlLocReader xmlLocReader, JsonMapper mapper, DoiProperties properties) {
		super(xmlLocReader, mapper, properties);
	}

	@Override
	public void handleMessages(DigitalSpecimenEvent digitalSpecimenEvent) {
		var dcRequest = buildDcRequest(digitalSpecimenEvent.pidRecord());
		log.debug("Test profile: skipping publication of specimen record, {}", dcRequest);
	}

	@Override
	public void tombstoneRecord(TombstoneEvent event) {
		var emptyTombstoneAttributes = new DcAttributes("", "", List.of(), List.of(), 0, List.of(), List.of(),
				List.of(), List.of(), List.of(), null, List.of(), List.of(), "", null);
		var dcRequest = buildDataCiteTombstoneRequest(emptyTombstoneAttributes, event);
		log.debug("Test profile: skipping tombstoning of record, {}", dcRequest);
	}

	@Override
	public void handleMessages(DigitalMediaEvent digitalMediaEvent) {
		var dcRequest = buildDcRequest(digitalMediaEvent.pidRecord());
		log.debug("Test profile: skipping publication of media record, {}", dcRequest);
	}

}
