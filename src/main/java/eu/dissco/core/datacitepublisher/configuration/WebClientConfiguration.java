package eu.dissco.core.datacitepublisher.configuration;

import static lombok.Lombok.sneakyThrow;

import eu.dissco.core.datacitepublisher.Profiles;
import eu.dissco.core.datacitepublisher.client.DataCiteClient;
import eu.dissco.core.datacitepublisher.client.DoiClient;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteConflictException;
import eu.dissco.core.datacitepublisher.exceptions.DoiResolutionException;
import eu.dissco.core.datacitepublisher.properties.DataCiteConnectionProperties;
import eu.dissco.core.datacitepublisher.properties.DoiConnectionProperties;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
@Profile({ Profiles.PUBLISH, Profiles.WEB })
@Slf4j
public class WebClientConfiguration {

	private final DataCiteConnectionProperties dataciteProperties;

	private final DoiConnectionProperties doiConnProperties;

	@Bean
	public DataCiteClient dataCiteClient() {
		var restClient = RestClient.builder()
			.baseUrl(dataciteProperties.getEndpoint())
			.defaultHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.api+json")
			.defaultHeaders(header -> header.setBasicAuth(dataciteProperties.getRepositoryId(),
					dataciteProperties.getPassword()))
			.defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
				var body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
				var status = response.getStatusCode();
				log.error("An error has occurred with the datacite API. Status: {}, body: {}", status, body);
				if (HttpStatus.UNPROCESSABLE_CONTENT.equals(status) && isConflictException(body)) {
					throw sneakyThrow(new DataCiteConflictException("DOI has already been taken"));
				}
				if (HttpStatus.NOT_FOUND.equals(status)) {
					log.warn("Credentials may be incorrect");
				}
				throw sneakyThrow(new DataCiteApiException());
			})
			.build();
		var proxyFactory = HttpServiceProxyFactory.builder()
			.exchangeAdapter(RestClientAdapter.create(restClient))
			.build();
		return proxyFactory.createClient(DataCiteClient.class);
	}

	@Bean
	public DoiClient doiClient() {
		var restClient = RestClient.builder()
			// On status error, log the response and throw a WebProcessingFailedException
			.defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
				var body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
				log.error("Unable to communicate with the DOI service. Status: {}, Body: {}", response.getStatusCode(),
						body);
				throw sneakyThrow(new DoiResolutionException());
			})
			.baseUrl(doiConnProperties.getEndpoint())
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
				var body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
				var status = response.getStatusCode();
				log.error("An error has occurred with the DOI API. Status: {}, body: {}", status, body);
				throw sneakyThrow(new DoiResolutionException());
			})
			.build();
		var proxyFactory = HttpServiceProxyFactory.builder()
			.exchangeAdapter(RestClientAdapter.create(restClient))
			.build();
		return proxyFactory.createClient(DoiClient.class);
	}

	private static boolean isConflictException(String errorBody) {
		return errorBody.contains("This ID has already been taken");
	}

}
