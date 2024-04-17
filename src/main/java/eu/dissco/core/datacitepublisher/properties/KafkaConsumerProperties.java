package eu.dissco.core.datacitepublisher.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


@Data
@Validated
@ConfigurationProperties("kafka.consumer")
public class KafkaConsumerProperties {


  @NotBlank
  private String host;

  @NotBlank
  private String group;

  @NotBlank
  private String topic;

  @Positive
  private int batchSize = 5000;

}
