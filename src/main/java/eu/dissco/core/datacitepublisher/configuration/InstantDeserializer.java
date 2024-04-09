package eu.dissco.core.datacitepublisher.configuration;

import static eu.dissco.core.datacitepublisher.configuration.ApplicationConfig.FDO_FORMATTER;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InstantDeserializer  extends JsonDeserializer<Instant> {

  @Override
  public Instant deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
    try {
      return Instant.from(FDO_FORMATTER.parse(jsonParser.getText()));
    } catch (IOException e) {
      log.error("An error has occurred deserializing a date. More information: {}", e.getMessage());
      return null;
    }
  }

}
