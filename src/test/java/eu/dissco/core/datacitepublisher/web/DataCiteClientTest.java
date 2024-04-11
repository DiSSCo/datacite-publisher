package eu.dissco.core.datacitepublisher.web;

import static eu.dissco.core.datacitepublisher.TestUtils.MAPPER;
import static eu.dissco.core.datacitepublisher.TestUtils.givenSpecimenJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import okhttp3.mockwebserver.MockWebServer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;


@ExtendWith(MockitoExtension.class)
class DataCiteClientTest {

  private static MockWebServer mockDataCiteServer;

  private DataCiteClient dataCiteClient;

  @BeforeAll
  static void init() throws IOException {
    mockDataCiteServer = new MockWebServer();
    mockDataCiteServer.start();
  }

  @BeforeEach
  void setup() {
    var webClient = WebClient.create(
        String.format("http://%s:%s", mockDataCiteServer.getHostName(),
            mockDataCiteServer.getPort()));
    dataCiteClient = new DataCiteClient(webClient);
  }

  @AfterAll
  static void destroy() throws IOException {
    mockDataCiteServer.shutdown();
  }

  @Test
  void testPostDoi() throws Exception {
    // Given
    var request = givenSpecimenJson();
    var expected = MAPPER.createObjectNode().put("data", "yep");
    mockDataCiteServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value())
        .setBody(MAPPER.writeValueAsString(expected))
        .addHeader("Content-Type", "application/json"));

    // When
    var response = dataCiteClient.sendDoiRequest(request, HttpMethod.POST);

    // Then
    assertThat(response).isEqualTo(expected);
  }

  @Test
  void testRetries() {
    // Given
    var request = givenSpecimenJson();
    int requestCount = mockDataCiteServer.getRequestCount();

    mockDataCiteServer.enqueue(new MockResponse().setResponseCode(501));
    mockDataCiteServer.enqueue(new MockResponse().setResponseCode(501));
    mockDataCiteServer.enqueue(new MockResponse().setResponseCode(501));
    mockDataCiteServer.enqueue(new MockResponse().setResponseCode(501));

    // When
    assertThrows(DataCiteApiException.class,
        () -> dataCiteClient.sendDoiRequest(request, HttpMethod.POST));
    var newRequestCount = mockDataCiteServer.getRequestCount();

    // Then
    assertThat(newRequestCount - requestCount).isEqualTo(4);
  }

  @Test
  void testDoiAlreadyTaken() throws Exception {
    // Given
    var request = givenSpecimenJson();
    var response = MAPPER.createObjectNode().put("errors", "yep");
    mockDataCiteServer.enqueue(
        new MockResponse().setResponseCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .setBody(MAPPER.writeValueAsString(response))
            .addHeader("Content-Type", "application/json"));

    // When / Then
    assertThrows(DataCiteApiException.class,
        () -> dataCiteClient.sendDoiRequest(request, HttpMethod.POST));
  }

  @Test
  void testInterruptedException() throws Exception {
    var request = givenSpecimenJson();
    var expected = MAPPER.createObjectNode().put("data", "yep");
    mockDataCiteServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value())
        .setBody(MAPPER.writeValueAsString(expected))
        .addHeader("Content-Type", "application/json"));

    Thread.currentThread().interrupt();

    // When / Then
    assertThrows(DataCiteApiException.class,
        () -> dataCiteClient.sendDoiRequest(request, HttpMethod.POST));
  }

}
