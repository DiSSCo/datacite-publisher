package eu.dissco.core.datacitepublisher.web;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.core.datacitepublisher.Profiles;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import eu.dissco.core.datacitepublisher.exceptions.HandleResolutionException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

@RequiredArgsConstructor
@Component
@Slf4j
@Profile({Profiles.PUBLISH, Profiles.WEB})
public class HandleClient {

  @Qualifier("handle")
  private final WebClient webClient;

  public JsonNode resolveHandles(List<String> handles) throws HandleResolutionException {
    var response = webClient.method(HttpMethod.GET)
        .uri(uriBuilder -> uriBuilder
            .queryParam("handles", handles)
            .build())
        .retrieve()
        .bodyToMono(JsonNode.class)
        .retryWhen(
            Retry.fixedDelay(3, Duration.ofSeconds(2))
                .filter(WebClientUtils::is5xxServerError)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> new DataCiteApiException(
                    "External Service failed to process after max retries")));
    try {
      return response.toFuture().get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("An Interrupted Exception has occurred in communicating with the Handle Manager API.", e);
      throw new HandleResolutionException();
    } catch (ExecutionException e) {
      log.error("An execution Exception with the Handle API has occurred", e);
      throw new HandleResolutionException();
    }

  }

}
