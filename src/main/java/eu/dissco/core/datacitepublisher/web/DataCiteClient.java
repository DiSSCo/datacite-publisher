package eu.dissco.core.datacitepublisher.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.core.datacitepublisher.domain.datacite.DcAttributes;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@RequiredArgsConstructor
@Component
@Slf4j
public class DataCiteClient {

  @Qualifier(value = "datacite")
  private final WebClient webClient;
  @Qualifier("objectMapper")
  private final ObjectMapper mapper;

  public JsonNode sendDoiRequest(JsonNode requestBody, HttpMethod method, String doi)
      throws DataCiteApiException {
    String uri = method.equals(HttpMethod.PUT) ?
        "/" + doi :
        "";
    var response = webClient.method(method)
        .uri(uri)
        .body(BodyInserters.fromValue(requestBody))
        .retrieve()
        .bodyToMono(JsonNode.class)
        .retryWhen(
            Retry.fixedDelay(3, Duration.ofSeconds(2))
                .filter(WebClientUtils::is5xxServerError)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> new DataCiteApiException(
                    "External Service failed to process after max retries")));
    return getResponse(response);
  }

  public DcAttributes getDoiRecord(String doi) throws DataCiteApiException {
    var uri = "/" + doi;
    var response = webClient.get()
        .uri(uri)
        .retrieve()
        .bodyToMono(JsonNode.class)
        .retryWhen(
            Retry.fixedDelay(3, Duration.ofSeconds(2))
                .filter(WebClientUtils::is5xxServerError)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> new DataCiteApiException(
                    "External Service failed to process after max retries")));
    var jsonNodeResponse = getResponse(response);
    try {
      return mapper.treeToValue(jsonNodeResponse.get("data").get("attributes"), DcAttributes.class);
    } catch (JsonProcessingException e) {
      log.error("Unable to parse response from DataCite: {}", jsonNodeResponse, e);
      throw new DataCiteApiException("Unexpected response from DataCite");
    }
  }

  private JsonNode getResponse(Mono<JsonNode> response) throws DataCiteApiException {
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
