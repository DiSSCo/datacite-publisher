package eu.dissco.core.datacitepublisher.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import eu.dissco.core.datacitepublisher.exceptions.InvalidFdoProfileRecievedException;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class XmlLocReader {
  @Qualifier("xmlMapper")
  private final XmlMapper xmlMapper;

  public List<String> getLocationsFromXml(String xmlDoc) throws InvalidFdoProfileRecievedException {
    try {
      var locations = xmlMapper.readValue(xmlDoc, LocationParentXml.class);
      return locations.getLocation().stream().map(LocationXml::getHref).toList();
    } catch (JsonProcessingException e){
      log.error("Unable to parse 10320/loc field for fdo", e);
      throw new InvalidFdoProfileRecievedException();
    }
  }

  public static String getLandingPageLocation(List<String> locations, String targetLoc){
    for (var location : locations){
      if (location.contains(targetLoc)){
        return location;
      }
    }
    log.warn("Unable to find landing page location from 10320/loc in handle record. Using first value in field");
    return locations.getFirst();
  }

  @Getter
  @NoArgsConstructor
  static class LocationParentXml {
    @JacksonXmlCData
    @JacksonXmlElementWrapper(useWrapping = false)
    List<LocationXml> location;
  }
  @Setter
  static class LocationXml {
    @Getter
    String href;
    String id;
    String weight;
  }
}
