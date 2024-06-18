package eu.dissco.core.datacitepublisher.web;

import static eu.dissco.core.datacitepublisher.TestUtils.DOI;
import static eu.dissco.core.datacitepublisher.TestUtils.DOI_ALT;
import static eu.dissco.core.datacitepublisher.TestUtils.MAPPER;
import static eu.dissco.core.datacitepublisher.TestUtils.givenDigitalSpecimenPidRecord;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import eu.dissco.core.datacitepublisher.exceptions.HandleResolutionException;
import java.io.IOException;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class HandleClientTest {

  private static MockWebServer mockHandleServer;
  private HandleClient handleClient;

  @BeforeAll
  static void init() throws IOException {
    mockHandleServer = new MockWebServer();
    mockHandleServer.start();
  }


  @BeforeEach
  void setup() {
    ExchangeFilterFunction errorResponseFilter = ExchangeFilterFunction
        .ofResponseProcessor(WebClientUtils::exchangeFilterResponseProcessor);
    var client = WebClient.builder()
        .baseUrl(String.format("http://%s:%s", mockHandleServer.getHostName(),
            mockHandleServer.getPort()))
        .filter(errorResponseFilter)
        .build();
    handleClient = new HandleClient(client);
  }

  @AfterAll
  static void destroy() throws IOException {
    mockHandleServer.shutdown();
  }

  @Test
  void testResolveHandle() throws Exception {
    //
    mockHandleServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value())
        .setBody(MAPPER.writeValueAsString(givenDigitalSpecimenPidRecord()))
        .addHeader("Content-Type", "application/json"));

    // When
    var response = handleClient.resolveHandles(List.of(DOI, DOI_ALT));

    // Then
    assertThat(response).isEqualTo(givenDigitalSpecimenPidRecord());
  }
  
  @Test
  void testRetries(){
    // Given
    int requestCount = mockHandleServer.getRequestCount();
    mockHandleServer.enqueue(new MockResponse().setResponseCode(501));
    mockHandleServer.enqueue(new MockResponse().setResponseCode(501));
    mockHandleServer.enqueue(new MockResponse().setResponseCode(501));
    mockHandleServer.enqueue(new MockResponse().setResponseCode(501));

    // When
    assertThrows(HandleResolutionException.class,
        () -> handleClient.resolveHandles(List.of(DOI)));
    var newRequestCount = mockHandleServer.getRequestCount();

    // Then
    assertThat(newRequestCount - requestCount).isEqualTo(4);
  }

  @Test
  void testInterruptedException() {
    // Given
    mockHandleServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value())
        .addHeader("Content-Type", "application/json"));

    Thread.currentThread().interrupt();

    // When / Then
    assertThrows(HandleResolutionException.class,
        () -> handleClient.resolveHandles(List.of(DOI)));
  }

}
