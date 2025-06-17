package eu.dissco.core.datacitepublisher.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMqProperties {

  @NotBlank
  private String specimenDoiDlq = "specimen-doi-queue-dlq";

  @NotBlank
  private String mediaDoiDlq = "media-doi-queue-dlq";

  @NotBlank
  private String tombstoneDoiDlq = "tombstone-doi-queue-dlq";

  @NotBlank
  private String dlqExchangeName = "doi-exchange-dlq";


}
