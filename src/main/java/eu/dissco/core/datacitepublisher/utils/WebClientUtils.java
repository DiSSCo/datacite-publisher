package eu.dissco.core.datacitepublisher.utils;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import java.util.ArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

public class WebClientUtils {

  private WebClientUtils() {
  }

  public static boolean is5xxServerError(Throwable throwable) {
    return throwable instanceof WebClientResponseException webClientResponseException
        && webClientResponseException.getStatusCode().is5xxServerError();
  }

  public static Mono<ClientResponse> exchangeFilterResponseProcessor(ClientResponse response) {
    var status = response.statusCode();
    if (HttpStatus.UNPROCESSABLE_ENTITY.equals(status)) {
      return response.bodyToMono(JsonNode.class)
          .flatMap(body -> Mono.error(new DataCiteApiException(getErrorTitles(body))));
    }
    if (HttpStatus.NOT_FOUND.equals(status)){
      return response.bodyToMono(JsonNode.class)
          .flatMap(body -> Mono.error(new DataCiteApiException(getErrorTitles(body) + " DataCite credentials may be incorrect")));
    }
    return Mono.just(response);
  }

  private static String getErrorTitles(JsonNode body) {
    var errorTitles = new ArrayList<String>();
    var errors = body.get("errors");
    for (var error : errors) {
      var errorTitle = error.get("title").asText();
      if (errorTitle.contains("This DOI has already been taken")){
        errorTitles.add(errorTitle.replace("This DOI", "DOI " + error.get("uid").asText()));
      }
      else{
        errorTitles.add(errorTitle);
      }
    }
    return errorTitles.toString().replace("[", "").replace("]", "");
  }

}
