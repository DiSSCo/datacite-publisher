package eu.dissco.core.datacitepublisher.web;

import eu.dissco.core.datacitepublisher.Profiles;
import eu.dissco.core.datacitepublisher.client.DoiClient;
import eu.dissco.core.datacitepublisher.exceptions.DoiResolutionException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@RequiredArgsConstructor
@Component
@Slf4j
@Profile({Profiles.PUBLISH, Profiles.WEB})
public class DoiResolutionComponent {

  private final DoiClient doiClient;

  public JsonNode resolveDois(List<String> dois) throws DoiResolutionException {
    return doiClient.resolveDois(dois);
  }

}
