package eu.dissco.core.datacitepublisher.configuration;

import eu.dissco.core.datacitepublisher.Profiles;
import eu.dissco.core.datacitepublisher.properties.DataCiteConnectionProperties;
import eu.dissco.core.datacitepublisher.properties.HandleConnectionProperties;
import eu.dissco.core.datacitepublisher.web.WebClientUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
@Profile({Profiles.PUBLISH, Profiles.WEB})
public class DataCiteClientConfig {

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
    return WebClient.builder()
        .baseUrl(handleProperties.getEndpoint())
        .build();
  }

}
