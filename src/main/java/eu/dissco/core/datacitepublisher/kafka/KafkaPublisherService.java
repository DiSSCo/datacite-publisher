package eu.dissco.core.datacitepublisher.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaPublisherService {

  private static final String DLQ_SUBJECT = "doi-dlq";

  private final KafkaTemplate<String, String> kafkaTemplate;

  public final void sendDlq(String message)
  {
    kafkaTemplate.send(DLQ_SUBJECT, message);
  }

}
