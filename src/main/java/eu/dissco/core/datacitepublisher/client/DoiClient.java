package eu.dissco.core.datacitepublisher.client;

import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import tools.jackson.databind.JsonNode;

public interface DoiClient {

  @GetExchange
  JsonNode resolveDois(@RequestParam List<String> dois);

}
