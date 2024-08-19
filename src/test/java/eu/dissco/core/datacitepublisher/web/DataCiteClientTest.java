package eu.dissco.core.datacitepublisher.web;

import static eu.dissco.core.datacitepublisher.TestUtils.DOI;
import static eu.dissco.core.datacitepublisher.TestUtils.MAPPER;
import static eu.dissco.core.datacitepublisher.TestUtils.PID;
import static eu.dissco.core.datacitepublisher.TestUtils.givenSpecimenJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.core.datacitepublisher.domain.datacite.DcAttributes;
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
    dataCiteClient = new DataCiteClient(webClient, MAPPER);
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

  @Test
  void testGetDoiRecord() throws Exception {
    // Given
    mockDataCiteServer.enqueue(new MockResponse()
        .setResponseCode(HttpStatus.OK.value())
        .setBody(MAPPER.writeValueAsString(givenDataCiteResponse()))
        .addHeader("Content-Type", "application/json"));

    // When
    var result = dataCiteClient.getDoiRecord(DOI);

    // Then
    assertThat(result).isInstanceOf(DcAttributes.class);
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

  private JsonNode givenDataCiteResponse() throws JsonProcessingException {
    return MAPPER.readTree("""
        {
          "data": {
            "id": "",
            "type": "dois",
            "attributes": {
              "doi": "10.3535/qr1-p21-9fw",
              "prefix": "10.82621",
              "suffix": "y3w-byd-2cm",
              "identifiers": [
                {
                  "identifier": "dfc36908-9949-4438-893d-927425c789a4:01na82s61",
                  "identifierType": "primarySpecimenObjectId"
                },
                {
                  "identifier": "20.5000.1025/Y3W-BYD-2CM",
                  "identifierType": "Handle"
                }
              ],
              "alternateIdentifiers": [
                {
                  "alternateIdentifierType": "primarySpecimenObjectId",
                  "alternateIdentifier": "dfc36908-9949-4438-893d-927425c789a4:01na82s61"
                },
                {
                  "alternateIdentifierType": "Handle",
                  "alternateIdentifier": "20.5000.1025/Y3W-BYD-2CM"
                }
              ],
              "creators": [
                {
                  "name": "Digital System of Scientific Collections",
                  "nameIdentifiers": [
                    {
                      "schemeUri": "https://ror.org",
                      "nameIdentifier": "https://ror.org/0566bfb96",
                      "nameIdentifierScheme": "ROR"
                    }
                  ],
                  "affiliation": []
                }
              ],
              "titles": [
                {
                  "title": "Lamium amplexicaule"
                }
              ],
              "publisher": "Distributed System of Scientific Collections",
              "container": {},
              "publicationYear": 2023,
              "subjects": [
                {
                  "subject": "Botany",
                  "subjectScheme": "topicDiscipline"
                }
              ],
              "contributors": [
                {
                  "name": "United States Department of Agriculture",
                  "nameIdentifiers": [
                    {
                      "schemeUri": "https://ror.org",
                      "nameIdentifier": "https://ror.org/01na82s61",
                      "nameIdentifierScheme": "ROR"
                    }
                  ],
                  "affiliation": []
                }
              ],
              "dates": [
                {
                  "date": "2023-08-03",
                  "dateType": "Issued"
                }
              ],
              "language": null,
              "types": {
                "ris": "DATA",
                "bibtex": "misc",
                "citeproc": "dataset",
                "schemaOrg": "Dataset",
                "resourceType": "digitalSpecimen",
                "resourceTypeGeneral": "Dataset"
              },
              "relatedIdentifiers": [
                {
                  "relationType": "IsVariantFormOf",
                  "relatedIdentifier": "https://sandbox.dissco.tech/api/v1/specimens/20.5000.1025/Y3W-BYD-2CM",
                  "relatedIdentifierType": "URL"
                }
              ],
              "relatedItems": [],
              "sizes": [],
              "formats": [],
              "version": null,
              "rightsList": [],
              "descriptions": [
                {
                  "description": "Digital Specimen hosted at United States Department of Agriculture"
                }
              ],
              "geoLocations": [],
              "fundingReferences": [],
              "xml": "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPHJlc291cmNlIHhtbG5zOnhzaT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEtaW5zdGFuY2UiIHhtbG5zPSJodHRwOi8vZGF0YWNpdGUub3JnL3NjaGVtYS9rZXJuZWwtNCIgeHNpOnNjaGVtYUxvY2F0aW9uPSJodHRwOi8vZGF0YWNpdGUub3JnL3NjaGVtYS9rZXJuZWwtNCBodHRwOi8vc2NoZW1hLmRhdGFjaXRlLm9yZy9tZXRhL2tlcm5lbC00L21ldGFkYXRhLnhzZCI+CiAgPGlkZW50aWZpZXIgaWRlbnRpZmllclR5cGU9IkRPSSI+MTAuODI2MjEvWTNXLUJZRC0yQ008L2lkZW50aWZpZXI+CiAgPGNyZWF0b3JzPgogICAgPGNyZWF0b3I+CiAgICAgIDxjcmVhdG9yTmFtZT5EaWdpdGFsIFN5c3RlbSBvZiBTY2llbnRpZmljIENvbGxlY3Rpb25zPC9jcmVhdG9yTmFtZT4KICAgICAgPG5hbWVJZGVudGlmaWVyIG5hbWVJZGVudGlmaWVyU2NoZW1lPSJST1IiIHNjaGVtZVVSST0iaHR0cHM6Ly9yb3Iub3JnIj5odHRwczovL3Jvci5vcmcvMDU2NmJmYjk2PC9uYW1lSWRlbnRpZmllcj4KICAgIDwvY3JlYXRvcj4KICA8L2NyZWF0b3JzPgogIDx0aXRsZXM+CiAgICA8dGl0bGU+TGFtaXVtIGFtcGxleGljYXVsZTwvdGl0bGU+CiAgPC90aXRsZXM+CiAgPHB1Ymxpc2hlcj5EaXN0cmlidXRlZCBTeXN0ZW0gb2YgU2NpZW50aWZpYyBDb2xsZWN0aW9uczwvcHVibGlzaGVyPgogIDxwdWJsaWNhdGlvblllYXI+MjAyMzwvcHVibGljYXRpb25ZZWFyPgogIDxyZXNvdXJjZVR5cGUgcmVzb3VyY2VUeXBlR2VuZXJhbD0iRGF0YXNldCI+ZGlnaXRhbFNwZWNpbWVuPC9yZXNvdXJjZVR5cGU+CiAgPHN1YmplY3RzPgogICAgPHN1YmplY3Qgc3ViamVjdFNjaGVtZT0idG9waWNEaXNjaXBsaW5lIj5Cb3Rhbnk8L3N1YmplY3Q+CiAgPC9zdWJqZWN0cz4KICA8Y29udHJpYnV0b3JzPgogICAgPGNvbnRyaWJ1dG9yIGNvbnRyaWJ1dG9yVHlwZT0iT3RoZXIiPgogICAgICA8Y29udHJpYnV0b3JOYW1lPlVuaXRlZCBTdGF0ZXMgRGVwYXJ0bWVudCBvZiBBZ3JpY3VsdHVyZTwvY29udHJpYnV0b3JOYW1lPgogICAgICA8bmFtZUlkZW50aWZpZXIgbmFtZUlkZW50aWZpZXJTY2hlbWU9IlJPUiIgc2NoZW1lVVJJPSJodHRwczovL3Jvci5vcmciPmh0dHBzOi8vcm9yLm9yZy8wMW5hODJzNjE8L25hbWVJZGVudGlmaWVyPgogICAgPC9jb250cmlidXRvcj4KICA8L2NvbnRyaWJ1dG9ycz4KICA8ZGF0ZXM+CiAgICA8ZGF0ZSBkYXRlVHlwZT0iSXNzdWVkIj4yMDIzLTA4LTAzPC9kYXRlPgogIDwvZGF0ZXM+CiAgPGFsdGVybmF0ZUlkZW50aWZpZXJzPgogICAgPGFsdGVybmF0ZUlkZW50aWZpZXIgYWx0ZXJuYXRlSWRlbnRpZmllclR5cGU9InByaW1hcnlTcGVjaW1lbk9iamVjdElkIj5kZmMzNjkwOC05OTQ5LTQ0MzgtODkzZC05Mjc0MjVjNzg5YTQ6MDFuYTgyczYxPC9hbHRlcm5hdGVJZGVudGlmaWVyPgogICAgPGFsdGVybmF0ZUlkZW50aWZpZXIgYWx0ZXJuYXRlSWRlbnRpZmllclR5cGU9IkhhbmRsZSI+MjAuNTAwMC4xMDI1L1kzVy1CWUQtMkNNPC9hbHRlcm5hdGVJZGVudGlmaWVyPgogIDwvYWx0ZXJuYXRlSWRlbnRpZmllcnM+CiAgPHJlbGF0ZWRJZGVudGlmaWVycz4KICAgIDxyZWxhdGVkSWRlbnRpZmllciByZWxhdGVkSWRlbnRpZmllclR5cGU9IlVSTCIgcmVsYXRpb25UeXBlPSJJc1ZhcmlhbnRGb3JtT2YiPmh0dHBzOi8vc2FuZGJveC5kaXNzY28udGVjaC9hcGkvdjEvc3BlY2ltZW5zLzIwLjUwMDAuMTAyNS9ZM1ctQllELTJDTTwvcmVsYXRlZElkZW50aWZpZXI+CiAgPC9yZWxhdGVkSWRlbnRpZmllcnM+CiAgPHNpemVzLz4KICA8Zm9ybWF0cy8+CiAgPHZlcnNpb24vPgogIDxkZXNjcmlwdGlvbnM+CiAgICA8ZGVzY3JpcHRpb24gZGVzY3JpcHRpb25UeXBlPSJBYnN0cmFjdCI+RGlnaXRhbCBTcGVjaW1lbiBob3N0ZWQgYXQgVW5pdGVkIFN0YXRlcyBEZXBhcnRtZW50IG9mIEFncmljdWx0dXJlPC9kZXNjcmlwdGlvbj4KICA8L2Rlc2NyaXB0aW9ucz4KPC9yZXNvdXJjZT4K",
              "url": "https://sandbox.dissco.tech/ds/20.5000.1025/Y3W-BYD-2CM",
              "contentUrl": null,
              "metadataVersion": 0,
              "schemaVersion": "http://datacite.org/schema/kernel-4.4",
              "source": "api",
              "isActive": true,
              "state": "findable",
              "reason": null,
              "landingPage": {
                "url": "https://sandbox.dissco.tech/ds/20.5000.1025/Y3W-BYD-2CM",
                "error": "",
                "status": 200,
                "checked": "2023-08-29 07:02:28",
                "bodyHasPid": false,
                "citationDoi": null,
                "contentType": "text/html",
                "schemaOrgId": null,
                "dcIdentifier": null,
                "hasSchemaOrg": false,
                "redirectUrls": [],
                "redirectCount": 0,
                "downloadLatency": 1647
              },
              "viewCount": 0,
              "viewsOverTime": [],
              "downloadCount": 0,
              "downloadsOverTime": [],
              "referenceCount": 0,
              "citationCount": 0,
              "citationsOverTime": [],
              "partCount": 0,
              "partOfCount": 0,
              "versionCount": 0,
              "versionOfCount": 0,
              "created": "2023-08-03T14:21:00.000Z",
              "registered": "2023-08-03T14:21:00.000Z",
              "published": "2023",
              "updated": "2024-08-18T23:32:39.000Z"
            },
            "relationships": {
              "client": {
                "data": {
                  "id": "znlx.aadziy",
                  "type": "clients"
                }
              },
              "provider": {
                "data": {
                  "id": "znlx",
                  "type": "providers"
                }
              },
              "media": {
                "data": {
                  "id": "10.3535/qr1-p21-9fw",
                  "type": "media"
                }
              },
              "references": {
                "data": []
              },
              "citations": {
                "data": []
              },
              "parts": {
                "data": []
              },
              "partOf": {
                "data": []
              },
              "versions": {
                "data": []
              },
              "versionOf": {
                "data": []
              }
            }
          }
        }
        """);
  }

}
