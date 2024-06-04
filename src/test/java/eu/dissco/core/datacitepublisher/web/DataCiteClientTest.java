package eu.dissco.core.datacitepublisher.web;

import static eu.dissco.core.datacitepublisher.TestUtils.DOI;
import static eu.dissco.core.datacitepublisher.TestUtils.MAPPER;
import static eu.dissco.core.datacitepublisher.TestUtils.PID;
import static eu.dissco.core.datacitepublisher.TestUtils.givenSpecimenJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;


@ExtendWith(MockitoExtension.class)
class DataCiteClientTest {

  private static MockWebServer mockDataCiteServer;
  private DataCiteClient dataCiteClient;
  private static final String ALT_ERROR = "another error message";

  @BeforeAll
  static void init() throws IOException {
    mockDataCiteServer = new MockWebServer();
    mockDataCiteServer.start();
  }

  @BeforeEach
  void setup() {
    ExchangeFilterFunction errorResponseFilter = ExchangeFilterFunction
        .ofResponseProcessor(WebClientUtils::exchangeFilterResponseProcessor);

    var webClient = WebClient.builder()
        .baseUrl(String.format("http://%s:%s", mockDataCiteServer.getHostName(),
            mockDataCiteServer.getPort()))
        .filter(errorResponseFilter)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.api+json")
        .build();
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
    var response = dataCiteClient.sendDoiRequest(request, HttpMethod.POST, DOI);

    // Then
    assertThat(response).isEqualTo(expected);
  }

  @Test
  void testUpdateDoi() throws Exception {
    // Given
    var request = givenSpecimenJson();
    var expected = MAPPER.createObjectNode().put("data", "yep");
    mockDataCiteServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value())
        .setBody(MAPPER.writeValueAsString(expected))
        .addHeader("Content-Type", "application/json"));

    // When
    var response = dataCiteClient.sendDoiRequest(request, HttpMethod.PUT, DOI);

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
        () -> dataCiteClient.sendDoiRequest(request, HttpMethod.POST, DOI));
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
        () -> dataCiteClient.sendDoiRequest(request, HttpMethod.POST, DOI));
  }

  @Test
  void testInterruptedException() throws Exception {
    // Given
    var request = givenSpecimenJson();
    var expected = MAPPER.createObjectNode().put("data", "yep");
    mockDataCiteServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value())
        .setBody(MAPPER.writeValueAsString(expected))
        .addHeader("Content-Type", "application/json"));

    Thread.currentThread().interrupt();

    // When / Then
    assertThrows(DataCiteApiException.class,
        () -> dataCiteClient.sendDoiRequest(request, HttpMethod.POST, DOI));
  }

  @Test
  void testDataCiteConflict() throws Exception {
    // Given
    var request = givenSpecimenJson();
    mockDataCiteServer.enqueue(new MockResponse()
        .setResponseCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
        .setBody(givenDataCiteErrorResponse(true))
        .addHeader("Content-Type", "application/json"));
    var expectedMessage = "DOI " + PID + " has already been taken";

    // When
    var e = assertThrows(DataCiteApiException.class,
        () -> dataCiteClient.sendDoiRequest(request, HttpMethod.POST, DOI));

    assertThat(e.getMessage()).contains(expectedMessage);
  }

  @Test
  void testDataCiteNotFound() throws Exception {
    // Given
    var request = givenSpecimenJson();
    mockDataCiteServer.enqueue(new MockResponse()
        .setResponseCode(HttpStatus.NOT_FOUND.value())
        .setBody(givenDataCiteErrorResponse(false))
        .addHeader("Content-Type", "application/json"));
    var expectedMessage = ALT_ERROR + " DataCite credentials may be incorrect";

    // When
    var e = assertThrows(DataCiteApiException.class,
        () -> dataCiteClient.sendDoiRequest(request, HttpMethod.POST, DOI));

    assertThat(e.getMessage()).contains(expectedMessage);
  }

  @Test
  void testDataCiteOther() throws Exception {
    // Given
    var request = givenSpecimenJson();
    mockDataCiteServer.enqueue(new MockResponse()
        .setResponseCode(HttpStatus.BAD_REQUEST.value())
        .setBody(givenDataCiteErrorResponse(false))
        .addHeader("Content-Type", "application/json"));
    var expectedMessage = " 400 Bad Request from POST";

    // When
    var e = assertThrows(DataCiteApiException.class,
        () -> dataCiteClient.sendDoiRequest(request, HttpMethod.POST, DOI));

    assertThat(e.getMessage()).contains(expectedMessage);
  }

  private static String givenDataCiteErrorResponse(boolean conflict) throws Exception {
    ArrayNode errors = MAPPER.createArrayNode();
    ObjectNode message = MAPPER.createObjectNode();
    if (conflict) {
      message
          .put("title", "This DOI has already been taken")
          .put("uid", PID);
    } else {
      message
          .put("title", ALT_ERROR);
    }
    errors.add(message);
    return MAPPER.writeValueAsString(MAPPER.createObjectNode()
        .set("errors", errors));
  }

}
