package eu.dissco.core.datacitepublisher.configuration;

import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteConflictException;
import eu.dissco.core.datacitepublisher.exceptions.DoiResolutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;

@Slf4j
public class WebClientErrorHandling {

  private WebClientErrorHandling() {
  }

  private static final String ERRORS = "errors";

  public static Mono<ClientResponse> exchangeFilterResponseProcessorDoi(ClientResponse response) {
    var status = response.statusCode();
    if (status.is4xxClientError() || status.is5xxServerError()) {
      return response.bodyToMono(JsonNode.class)
          .flatMap(body -> {
            log.error("An error has occurred with the DOI service. Status code: {}, response: {}",
                status, body);
            return Mono.error(new DoiResolutionException());
          });
    }
    return Mono.just(response);
  }

  public static Mono<ClientResponse> exchangeFilterResponseProcessorDataCite(
      ClientResponse response) {
    var status = response.statusCode();
    if (status.is4xxClientError() || status.is5xxServerError()) {
      if (HttpStatus.UNPROCESSABLE_CONTENT.equals(status)) {
        return response.bodyToMono(JsonNode.class)
            .flatMap(body -> {
              log.error("An error has occurred with the api: {}", body);
              if (isConflictException(body)) {
                return Mono.error(new DataCiteConflictException("ID has already been taken"));
              }
              return Mono.error(new DataCiteApiException());
            });
      }
      if (HttpStatus.NOT_FOUND.equals(status)) {
        return response.bodyToMono(JsonNode.class)
            .flatMap(body -> {
              log.error("credentials may be incorrect: {}", body);
              return Mono.error(new DataCiteApiException());
            });
      }
      return response.bodyToMono(JsonNode.class)
          .flatMap(body -> {
            log.error("An error has occurred with the Datacite API: {}", body);
            return Mono.error(new DataCiteApiException());
          });
    }
    return Mono.just(response);
  }

  private static boolean isConflictException(JsonNode errorBody) {
    if (errorBody.has(ERRORS) && errorBody.get(ERRORS).isArray()) {
      for (JsonNode error : errorBody.get(ERRORS)) {
        if ("This ID has already been taken".equals(error.get("title").asString())) {
          return true;
        }
      }
    }
    return false;
  }


}
