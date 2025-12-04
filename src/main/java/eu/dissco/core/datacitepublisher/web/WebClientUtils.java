package eu.dissco.core.datacitepublisher.web;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteConflictException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
public class WebClientUtils {

  private WebClientUtils() {
  }

  private static final String ERRORS = "errors";

  public static boolean is5xxServerError(Throwable throwable) {
    return throwable instanceof WebClientResponseException webClientResponseException
        && webClientResponseException.getStatusCode().is5xxServerError();
  }

  public static Mono<ClientResponse> exchangeFilterResponseProcessor(ClientResponse response) {
    var status = response.statusCode();
    if (HttpStatus.UNPROCESSABLE_ENTITY.equals(status)) {
      return response.bodyToMono(JsonNode.class)
          .flatMap(body -> {
            log.error("An error has occurred with the datacite api: {}", body);
            if (isConflictException(body)) {
              return Mono.error(new DataCiteConflictException("DOI has already been taken"));
            }
            return Mono.error(new DataCiteApiException());
          });
    }
    if (HttpStatus.NOT_FOUND.equals(status)){
      return response.bodyToMono(JsonNode.class)
          .flatMap(body -> {
            log.error("Datacite credentials may be incorrect: {}", body);
            return Mono.error(new DataCiteApiException());
          });
    }
    return Mono.just(response);
  }

  private static boolean isConflictException(JsonNode errorBody) {
    if (errorBody.has(ERRORS) && errorBody.get(ERRORS).isArray()) {
      for (JsonNode error : errorBody.get(ERRORS)) {
        if ("This DOI has already been taken".equals(error.get("title").asText())) {
          return true;
        }
      }
    }
    return false;
  }


}
