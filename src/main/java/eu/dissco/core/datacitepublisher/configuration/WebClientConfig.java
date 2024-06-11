package eu.dissco.core.datacitepublisher.configuration;

import eu.dissco.core.datacitepublisher.properties.DataCiteConnectionProperties;
import eu.dissco.core.datacitepublisher.properties.HandleConnectionProperties;
import eu.dissco.core.datacitepublisher.web.WebClientUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

  private final DataCiteConnectionProperties dataciteProperties;
  private final HandleConnectionProperties handleProperties;

  @Bean("datacite")
  public WebClient dataciteClient() {
    ExchangeFilterFunction errorResponseFilter = ExchangeFilterFunction
        .ofResponseProcessor(WebClientUtils::exchangeFilterResponseProcessor);
    return WebClient.builder()
        .baseUrl(dataciteProperties.getEndpoint())
        .filter(errorResponseFilter)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.api+json")
        .defaultHeaders(
            header -> header.setBasicAuth(dataciteProperties.getRepositoryId(), dataciteProperties.getPassword()))
        .build();
  }

  @Bean("handle")
  public WebClient handleClient() {
    ExchangeFilterFunction errorResponseFilter = ExchangeFilterFunction
        .ofResponseProcessor(WebClientUtils::exchangeFilterResponseProcessor);
    return WebClient.builder()
        .baseUrl(handleProperties.getEndpoint())
        .filter(errorResponseFilter)
        .build();
  }

}
