package eu.dissco.core.datacitepublisher.web;

import eu.dissco.core.datacitepublisher.Profiles;
import eu.dissco.core.datacitepublisher.client.DataCiteClient;
import eu.dissco.core.datacitepublisher.domain.datacite.DcAttributes;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
@Component
@Slf4j
@Profile({ Profiles.PUBLISH, Profiles.WEB })
public class DataCiteComponent {

	private final DataCiteClient dataCiteClient;

	private final JsonMapper mapper;

	public void createNewDataCiteRecord(JsonNode requestBody, String doi) throws DataCiteApiException {
		log.debug("Sending post request to DataCite with method: with doi: {} and  body: {}", doi, requestBody);
		dataCiteClient.postDoi(requestBody);
	}

	public void updateDataCiteRecord(JsonNode requestBody, String doi) throws DataCiteApiException {
		log.debug("Sending update request to DataCite with method: with doi: {} and  body: {}", doi, requestBody);
		dataCiteClient.updateDoi(doi, requestBody);
	}

	public DcAttributes getDataCiteRecord(String doi) {
		var response = dataCiteClient.getDataCiteRecord(doi);
		return mapper.treeToValue(response.get("data").get("attributes"), DcAttributes.class);
	}

}
