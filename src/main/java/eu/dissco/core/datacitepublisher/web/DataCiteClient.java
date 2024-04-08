package eu.dissco.core.datacitepublisher.web;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

@RequiredArgsConstructor
@Component
@Slf4j
public class DataCiteClient {

  private final WebClient webClient;

  public JsonNode sendDoiRequest(JsonNode requestBody) throws DataCiteException {
    var response = webClient.method(HttpMethod.POST)
        .body(BodyInserters.fromValue(requestBody))
        .retrieve()
        .bodyToMono(JsonNode.class)
        .retryWhen(
            Retry.fixedDelay(3, Duration.ofSeconds(2))
                .filter(WebClientUtils::is5xxServerError)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> new DataCiteException(
                    "External Service failed to process after max retries")));
    try {
      return response.toFuture().get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new DataCiteException(
          "An Interrupted Exception has occurred in communicating with the DataCite API.");
    } catch (ExecutionException e) {
      log.error("An execution Exception with the DataCite API has occurred", e);
      throw new DataCiteException(e.getCause().getMessage());
    }
  }

}
