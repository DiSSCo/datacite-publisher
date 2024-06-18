package eu.dissco.core.datacitepublisher.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties("handle")
public class HandleConnectionProperties {

  @NotBlank
  private String endpoint = "https://sandbox.dissco.tech/handle-manager/api/v1/pids/records";

  @NotNull
  private int maxHandles = 100;

}
