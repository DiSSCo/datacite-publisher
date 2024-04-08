package eu.dissco.core.datacitepublisher.configuration;

import static eu.dissco.core.datacitepublisher.configuration.ApplicationConfig.FORMATTER;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InstantSerializer extends JsonSerializer<Instant> {

  @Override
  public void serialize(Instant value, JsonGenerator jsonGenerator,
      SerializerProvider serializerProvider) {
    try {
      jsonGenerator.writeString(FORMATTER.format(value));
    } catch (IOException e) {
      log.error("An error has occurred serializing a date. More information: {}", e.getMessage());
    }
  }

}
