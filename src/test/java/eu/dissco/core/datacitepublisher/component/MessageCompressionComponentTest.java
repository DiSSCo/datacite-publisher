package eu.dissco.core.datacitepublisher.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;

@ExtendWith(MockitoExtension.class)
class MessageCompressionComponentTest {

  private MessageCompressionComponent messageCompressionComponent;

  @BeforeEach
  void setUp() {
    messageCompressionComponent = new MessageCompressionComponent();
  }

  @Test
  void testCompressMessage() {
    // Given
    var messageString = givenMessage();

    // When
    var compressedMessage = messageCompressionComponent.toMessage(messageString,
        new MessageProperties());

    // Then
    assertThat(compressedMessage.getMessageProperties().getContentEncoding()).isEqualTo("gzip");
    assertThat(compressedMessage.getMessageProperties().getContentType()).isEqualTo(
        "application/json");
    var decompressedMessage = messageCompressionComponent.fromMessage(compressedMessage);
    assertThat(decompressedMessage).isEqualTo(messageString);
  }

  @Test
  void testInvalidMessage() {
    // Given
    var messageInteger = 1234;
    var messageProperties = new MessageProperties();

    // When / Then
    assertThrows(MessageConversionException.class,
        () -> messageCompressionComponent.toMessage(messageInteger, messageProperties));
  }

  @Test
  void testPlainMessage() {
    // Given
    var messageProperties = new MessageProperties();
    messageProperties.setContentType("text/plain");
    var message = new Message(givenMessage().getBytes(StandardCharsets.UTF_8), messageProperties);

    // When
    var result = messageCompressionComponent.fromMessage(message);

    // Then
    assertThat(result).isEqualTo(givenMessage());
  }

  private String givenMessage() {
    return """
        {
          "enrichmentList": [
            "OCR"
            ],
          "digitalSpecimenWrapper": {
            "ods:normalisedPhysicalSpecimenID": "https://geocollections.info/specimen/23602",
            "ods:type": "https://doi.org/21.T11148/894b1e6cad57e921764e",
            "ods:attributes": {
              "ods:physicalSpecimenIDType": "Global",
              "ods:physicalSpecimenID":"https://geocollections.info/specimen/23602",
              "ods:organisationID": "https://ror.org/0443cwa12",
              "ods:organisationName": "National Museum of Natural History",
              "ods:normalisedPhysicalSpecimenID": "https://geocollections.info/specimen/23602",
              "ods:specimenName": "Biota",
              "dwc:datasetName": null,
              "dwc:collectionID": null,
              "ods:sourceSystemID": "https://hdl.handle.net/TEST/57Z-6PC-64W",
              "ods:sourceSystemName": "A very nice source system",
              "dcterms:license": "http://creativecommons.org/licenses/by-nc/4.0/",
              "dcterms:modified": "2022-11-01T09:59:24.000Z",
              "ods:topicDiscipline": "Botany",
              "ods:isMarkedAsType": true,
              "ods:isKnownToContainMedia": false,
              "ods:livingOrPreserved": "Preserved"
            },
            "ods:originalAttributes": {
                "abcd:unitID": "152-4972",
                "abcd:sourceID": "GIT",
                "abcd:unitGUID": "https://geocollections.info/specimen/23646",
                "abcd:recordURI": "https://geocollections.info/specimen/23646",
                "abcd:recordBasis": "FossilSpecimen",
                "abcd:unitIDNumeric": 23646,
                "abcd:dateLastEdited": "2004-06-09T10:17:54.000+00:00",
                "abcd:kindOfUnit/0/value": "",
                "abcd:sourceInstitutionID": "Department of Geology, TalTech",
                "abcd:kindOfUnit/0/language": "en",
                "abcd:gathering/country/name/value": "Estonia",
                "abcd:gathering/localityText/value": "Laeva 297 borehole",
                "abcd:gathering/country/iso3166Code": "EE",
                "abcd:gathering/localityText/language": "en",
                "abcd:gathering/altitude/measurementOrFactText/value": "39.9",
                "abcd:identifications/identification/0/preferredFlag": true,
                "abcd:gathering/depth/measurementOrFactAtomised/lowerValue/value": "165",
                "abcd:gathering/depth/measurementOrFactAtomised/unitOfMeasurement": "m",
                "abcd:gathering/siteCoordinateSets/siteCoordinates/0/coordinatesLatLong/spatialDatum": "WGS84",
                "abcd:gathering/stratigraphy/chronostratigraphicTerms/chronostratigraphicTerm/0/term": "Pirgu Stage",
                "abcd:gathering/stratigraphy/chronostratigraphicTerms/chronostratigraphicTerm/1/term": "Katian",
                "abcd:gathering/siteCoordinateSets/siteCoordinates/0/coordinatesLatLong/latitudeDecimal": 58.489269,
                "abcd:gathering/siteCoordinateSets/siteCoordinates/0/coordinatesLatLong/longitudeDecimal": 26.385719,
                "abcd:gathering/stratigraphy/chronostratigraphicTerms/chronostratigraphicTerm/0/language": "en",
                "abcd:gathering/stratigraphy/chronostratigraphicTerms/chronostratigraphicTerm/1/language": "en",
                "abcd:identifications/identification/0/result/taxonIdentified/scientificName/fullScientificNameString": "Biota",
                "abcd-efg:earthScienceSpecimen/unitStratigraphicDetermination/chronostratigraphicAttributions/chronostratigraphicAttribution/0/chronostratigraphicName": "Pirgu Stage",
                "abcd-efg:earthScienceSpecimen/unitStratigraphicDetermination/chronostratigraphicAttributions/chronostratigraphicAttribution/0/chronoStratigraphicDivision": "Stage"
              }
          },
          "digitalMediaEvents": []
        }""";
  }
}
