package eu.dissco.core.datacitepublisher.properties;

import eu.dissco.core.datacitepublisher.Profiles;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties("datacite")
@Profile(Profiles.PUBLISH)
public class DataCiteConnectionProperties {

  @NotBlank
  private String repositoryId;

  @NotBlank
  private String password;

  @NotBlank
  private String endpoint;
}
