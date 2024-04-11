package eu.dissco.core.datacitepublisher.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaPublisherService {

  private static final String DLQ_SUBJECT = "doi-dlq";

  private final KafkaTemplate<String, String> kafkaTemplate;

  public final void sendDlq(String message){
    kafkaTemplate.send(message, DLQ_SUBJECT);
  }

}