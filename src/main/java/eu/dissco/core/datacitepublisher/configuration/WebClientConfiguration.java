package eu.dissco.core.datacitepublisher.configuration;

import eu.dissco.core.datacitepublisher.Profiles;
import eu.dissco.core.datacitepublisher.client.DataCiteClient;
import eu.dissco.core.datacitepublisher.client.DoiClient;
import eu.dissco.core.datacitepublisher.properties.DataCiteConnectionProperties;
import eu.dissco.core.datacitepublisher.properties.DoiConnectionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
@Profile({ Profiles.PUBLISH, Profiles.WEB })
public class WebClientConfiguration {

	private final DataCiteConnectionProperties dataciteProperties;

	private final DoiConnectionProperties doiConnProperties;

	@Bean
	public DataCiteClient dataCiteClient() {
		ExchangeFilterFunction errorResponseFilter = ExchangeFilterFunction
			.ofResponseProcessor(WebClientErrorHandling::exchangeFilterResponseProcessorDataCite);
		var webClient = WebClient.builder()
			.baseUrl(dataciteProperties.getEndpoint())
			.filter(errorResponseFilter)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.api+json")
			.defaultHeaders(header -> header.setBasicAuth(dataciteProperties.getRepositoryId(),
					dataciteProperties.getPassword()))
			.build();
		var proxyFactory = HttpServiceProxyFactory.builder()
			.exchangeAdapter(WebClientAdapter.create(webClient))
			.build();
		return proxyFactory.createClient(DataCiteClient.class);
	}

	@Bean
	public DoiClient doiClient() {
		ExchangeFilterFunction errorResponseFilter = ExchangeFilterFunction
			.ofResponseProcessor(WebClientErrorHandling::exchangeFilterResponseProcessorDoi);
		var webClient = WebClient.builder()
			.baseUrl(doiConnProperties.getEndpoint())
			.filter(errorResponseFilter)
			.build();
		var proxyFactory = HttpServiceProxyFactory.builder()
			.exchangeAdapter(WebClientAdapter.create(webClient))
			.build();
		return proxyFactory.createClient(DoiClient.class);
	}

}
