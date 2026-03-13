package eu.dissco.core.datacitepublisher.client;

import eu.dissco.core.datacitepublisher.exceptions.DoiResolutionException;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import tools.jackson.databind.JsonNode;

public interface DoiClient {

  @GetExchange("records")
  JsonNode resolveDois(@RequestParam List<String> dois) throws DoiResolutionException;

}
