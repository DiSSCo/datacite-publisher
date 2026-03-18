package eu.dissco.core.datacitepublisher.configuration;

import static eu.dissco.core.datacitepublisher.TestUtils.MAPPER;
import static org.mockito.BDDMockito.given;

import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteConflictException;
import eu.dissco.core.datacitepublisher.exceptions.DoiResolutionException;
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
class WebClientErrorHandlingTest {

	@Mock
	private ClientResponse clientResponse;

	@Mock
	private WebClientResponseException webClientResponseException;

	@Test
	void testExchangeFilterDataCiteOk() {
		// Given
		given(clientResponse.statusCode()).willReturn(HttpStatus.OK);

		// When
		Mono<ClientResponse> result = WebClientErrorHandling.exchangeFilterResponseProcessorDataCite(clientResponse);

		// Then
		StepVerifier.create(result).expectNext(clientResponse).verifyComplete();
	}

	@Test
	void testExchangeFilterDataCiteNotFound() {
		// Given
		var body = MAPPER.readTree("{\"message\":\"Not found details\"}");
		given(clientResponse.statusCode()).willReturn(HttpStatus.NOT_FOUND);
		given(clientResponse.bodyToMono(JsonNode.class)).willReturn(Mono.just(body));

		// When
		Mono<ClientResponse> result = WebClientErrorHandling.exchangeFilterResponseProcessorDataCite(clientResponse);

		// Then
		StepVerifier.create(result).expectError(DataCiteApiException.class).verify();
	}

	@Test
	void testExchangeFilterDataCiteUnprocessable() {
		// Given
		var body = MAPPER.readTree("{\"message\":\"Some Unprocessable Error\"}");
		given(clientResponse.statusCode()).willReturn(HttpStatus.UNPROCESSABLE_CONTENT);
		given(clientResponse.bodyToMono(JsonNode.class)).willReturn(Mono.just(body));

		// When
		Mono<ClientResponse> result = WebClientErrorHandling.exchangeFilterResponseProcessorDataCite(clientResponse);

		// Then
		StepVerifier.create(result).expectError(DataCiteApiException.class).verify();
	}

	@Test
	void testExchangeFilterDataCiteConflict() {
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
		Mono<ClientResponse> result = WebClientErrorHandling.exchangeFilterResponseProcessorDataCite(clientResponse);

		// Then
		StepVerifier.create(result).expectError(DataCiteConflictException.class).verify();
	}

	@Test
	void testExchangeFilterDoi4xxError() {
		// Given
		var body = MAPPER.readTree("{\"message\":\"Some Unprocessable Error\"}");
		given(clientResponse.statusCode()).willReturn(HttpStatus.NOT_FOUND);
		given(clientResponse.bodyToMono(JsonNode.class)).willReturn(Mono.just(body));

		// When
		Mono<ClientResponse> result = WebClientErrorHandling.exchangeFilterResponseProcessorDoi(clientResponse);

		// Then
		StepVerifier.create(result).expectError(DoiResolutionException.class).verify();
	}

	@Test
	void testExchangeFilterDoi5xxError() {
		// Given
		var body = MAPPER.readTree("{\"message\":\"Some Unprocessable Error\"}");
		given(clientResponse.statusCode()).willReturn(HttpStatus.INTERNAL_SERVER_ERROR);
		given(clientResponse.bodyToMono(JsonNode.class)).willReturn(Mono.just(body));

		// When
		Mono<ClientResponse> result = WebClientErrorHandling.exchangeFilterResponseProcessorDoi(clientResponse);

		// Then
		StepVerifier.create(result).expectError(DoiResolutionException.class).verify();
	}

	@Test
	void testExchangeFilterDoiOk() {
		// Given
		given(clientResponse.statusCode()).willReturn(HttpStatus.OK);

		// When
		Mono<ClientResponse> result = WebClientErrorHandling.exchangeFilterResponseProcessorDoi(clientResponse);

		// Then
		StepVerifier.create(result).expectNext(clientResponse).verifyComplete();
	}

}
