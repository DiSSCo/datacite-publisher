package eu.dissco.core.datacitepublisher.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties("doi")
public class DoiProperties {
  @NotBlank
  private String prefix;

}
