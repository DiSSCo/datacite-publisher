package eu.dissco.core.datacitepublisher.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import eu.dissco.core.datacitepublisher.exceptions.FdoProfileException;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XmlLocReader {
  private XmlLocReader(){}

  public static List<String> getLocationsFromXml(String xmlDoc, String doi) throws FdoProfileException {
    try {
      var xmlMapper = new XmlMapper();
      var locations = xmlMapper.readValue(xmlDoc, LocationParentXml.class);
      return locations.getLocation().stream().map(LocationXml::getHref).toList();
    } catch (JsonProcessingException e){
      log.error("Unable to parse 10320/loc field");
      throw new FdoProfileException("10320/loc", xmlDoc, doi);
    }
  }

  public static String getLandingPageLocation(List<String> locations, String targetLoc){
    for (var location : locations){
      if (location.contains(targetLoc)){
        return location;
      }
    }
    log.error("Unable to find landing page location from 10320/loc in handle record. Using first value in field");
    return locations.getFirst();
  }

  @Data
  protected static class LocationParentXml {
    @JacksonXmlCData
    @JacksonXmlElementWrapper(useWrapping = false)
    List<LocationXml> location;
  }
  @Setter
  protected static class LocationXml {
    @Getter
    String href;
    String id;
    String weight;
  }
}
