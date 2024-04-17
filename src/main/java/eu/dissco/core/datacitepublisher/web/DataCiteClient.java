package eu.dissco.core.datacitepublisher.web;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
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

  public JsonNode sendDoiRequest(JsonNode requestBody, HttpMethod method) throws DataCiteApiException {
    var response = webClient.method(method)
        .body(BodyInserters.fromValue(requestBody))
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
      log.error("An Interrupted Exception has occurred in communicating with the DataCite API.", e);
      throw new DataCiteApiException(e.getMessage());
    } catch (ExecutionException e) {
      log.error("An execution Exception with the DataCite API has occurred", e);
      throw new DataCiteApiException(e.getLocalizedMessage());
    }
  }
}
