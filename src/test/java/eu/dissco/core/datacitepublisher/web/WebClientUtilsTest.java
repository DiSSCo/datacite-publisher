package eu.dissco.core.datacitepublisher.web;

import static eu.dissco.core.datacitepublisher.TestUtils.MAPPER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.JsonNode;

@ExtendWith(MockitoExtension.class)
class WebClientUtilsTest {

  @Mock
  private ClientResponse clientResponse;

  @Mock
  private WebClientResponseException webClientResponseException;

  @Test
  void testIs5xxErrorTrue() {
    // Given
    given(webClientResponseException.getStatusCode()).willReturn(HttpStatus.INTERNAL_SERVER_ERROR);

    // When
    boolean result = WebClientUtils.is5xxServerError(webClientResponseException);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void testIs5xErrorOtherKindOfError() {
    // Given
    given(webClientResponseException.getStatusCode()).willReturn(HttpStatus.BAD_REQUEST);

    // When
    boolean result = WebClientUtils.is5xxServerError(webClientResponseException);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void testIs5xxErrorOk() {
    // Given
    given(webClientResponseException.getStatusCode()).willReturn(HttpStatus.OK);

    // When
    boolean result = WebClientUtils.is5xxServerError(webClientResponseException);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void testExchangeFilterOk() {
    // Given
    given(clientResponse.statusCode()).willReturn(HttpStatus.OK);

    // When
    Mono<ClientResponse> result = WebClientUtils.exchangeFilterResponseProcessor(clientResponse);

    // Then
    StepVerifier.create(result)
        .expectNext(clientResponse)
        .verifyComplete();
  }

  @Test
  void testExchangeFilterNotFound() {
    // Given
    var body = MAPPER.readTree("{\"message\":\"Not found details\"}");
    given(clientResponse.statusCode()).willReturn(HttpStatus.NOT_FOUND);
    given(clientResponse.bodyToMono(JsonNode.class)).willReturn(Mono.just(body));

    // When
    Mono<ClientResponse> result = WebClientUtils.exchangeFilterResponseProcessor(clientResponse);

    // Then
    StepVerifier.create(result)
        .expectError(DataCiteApiException.class)
        .verify();
  }

  @Test
  void testExchangeFilterUnprocessable() {
    // Given
    var body = MAPPER.readTree("{\"message\":\"Some Unprocessable Error\"}");
    given(clientResponse.statusCode()).willReturn(HttpStatus.UNPROCESSABLE_CONTENT);
    given(clientResponse.bodyToMono(JsonNode.class)).willReturn(Mono.just(body));

    // When
    Mono<ClientResponse> result = WebClientUtils.exchangeFilterResponseProcessor(clientResponse);

    // Then
    StepVerifier.create(result)
        .expectError(DataCiteApiException.class)
        .verify();
  }

  @Test
  void testExchangeFilterConflict() {
    // Given
    var body = MAPPER.readTree("""
        {
          "errors" : [
            {
              "title": "error 1"
            },
            {
              "title": "This ID has already been taken"
            }
          ]
        }
        """);
    given(clientResponse.statusCode()).willReturn(HttpStatus.UNPROCESSABLE_CONTENT);
    given(clientResponse.bodyToMono(JsonNode.class)).willReturn(Mono.just(body));

    // When
    Mono<ClientResponse> result = WebClientUtils.exchangeFilterResponseProcessor(clientResponse);

    // Then
    StepVerifier.create(result)
        .expectError(DataCiteConflictException.class)
        .verify();
  }

}
