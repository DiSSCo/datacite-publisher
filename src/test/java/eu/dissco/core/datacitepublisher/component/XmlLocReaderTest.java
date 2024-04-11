package eu.dissco.core.datacitepublisher.component;

import static eu.dissco.core.datacitepublisher.TestUtils.LOCS;
import static eu.dissco.core.datacitepublisher.TestUtils.LOCS_ARR;
import static eu.dissco.core.datacitepublisher.TestUtils.PID;
import static eu.dissco.core.datacitepublisher.TestUtils.XML_MAPPER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import eu.dissco.core.datacitepublisher.exceptions.InvalidFdoProfileRecievedException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class XmlLocReaderTest {

  private XmlLocReader xmlLocReader;

  @BeforeEach
  void init() {
    xmlLocReader = new XmlLocReader(XML_MAPPER);
  }

  @Test
  void testGetLocationsFromXml() throws Exception {
    // When
    var result = xmlLocReader.getLocationsFromXml(LOCS);

    // Then
    assertThat(result).hasSameElementsAs(LOCS_ARR);
  }

  @Test
  void testGetLocationsBadLocs() {
    // When / Then
    assertThrows(InvalidFdoProfileRecievedException.class,
        () -> xmlLocReader.getLocationsFromXml("bad document"));
  }

  @Test
  void testGetLandingPageLocation() {
    // Given
    var targetPage = "https://sandbox.dissco.tech/ds/";
    var expected = targetPage + PID;
    var locsArr = List.of("https://sandbox.dissco.tech/specimens/api/v1/" + PID, expected);

    // When
    var result = XmlLocReader.getLandingPageLocation(locsArr, targetPage);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testGetLandingPageLocationDefaultToFirst() {
    // Given

    var locsArr = List.of("https://sandbox.dissco.tech/specimens/api/v1/" + PID,
        "https://sandbox.dissco.tech/ds/" + PID);

    // When
    var result = XmlLocReader.getLandingPageLocation(locsArr, "otherTarget");

    // Then
    assertThat(result).isEqualTo(locsArr.getFirst());
  }


}
