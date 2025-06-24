package eu.dissco.core.datacitepublisher.properties;

import eu.dissco.core.datacitepublisher.Profiles;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties("handle")
@Profile(Profiles.PUBLISH)
public class HandleConnectionProperties {

  @NotBlank
  private String endpoint;

  @NotNull
  private int maxHandles = 100;

}
