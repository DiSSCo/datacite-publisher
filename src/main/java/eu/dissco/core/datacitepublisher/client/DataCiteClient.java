package eu.dissco.core.datacitepublisher.client;

import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;
import tools.jackson.databind.JsonNode;

public interface DataCiteClient {

  @PostExchange("")
  void postDoi(@RequestBody JsonNode requestBody) throws DataCiteApiException;

  @PutExchange("{doi}")
  void updateDoi(@RequestParam String doi, @RequestBody JsonNode requestBody)
      throws DataCiteApiException;

  @GetExchange("{doi}/?publisher=true")
  JsonNode getDataCiteRecord(@RequestParam String doi);

}
