package eu.dissco.core.datacitepublisher.service;

import eu.dissco.core.datacitepublisher.Profiles;
import eu.dissco.core.datacitepublisher.domain.DigitalMediaEvent;
import eu.dissco.core.datacitepublisher.domain.DigitalSpecimenEvent;
import eu.dissco.core.datacitepublisher.domain.TombstoneEvent;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
@Slf4j
@RequiredArgsConstructor
@Profile({ Profiles.PUBLISH, Profiles.TEST })
public class RabbitMqConsumerService {

	private final JsonMapper mapper;

	private final DataCiteService service;

	@RabbitListener(queues = "${rabbitmq.specimen-doi-queue-name:specimen-doi-queue}",
			containerFactory = "consumerBatchContainerFactory")
	public void getSpecimenMessages(@Payload String message) throws DataCiteApiException {
		var event = mapper.readValue(message, DigitalSpecimenEvent.class);
		log.info("Received {} specimen message", event.eventType());
		service.handleMessages(event);
		log.info("Successfully processed event for specimen {}", event.pidRecord().getPid());
	}

	@RabbitListener(queues = "${rabbitmq.media-doi-queue-name:media-doi-queue}",
			containerFactory = "consumerBatchContainerFactory")
	public void getMediaMessages(@Payload String message) throws DataCiteApiException {
		var event = mapper.readValue(message, DigitalMediaEvent.class);
		log.info("Received {} media message", event.eventType());
		service.handleMessages(event);
		log.info("Successfully processed event for media {}", event.pidRecord().getPid());
	}

	@RabbitListener(queues = "${rabbitmq.tombstone-doi-queue-name:tombstone-doi-queue}",
			containerFactory = "consumerBatchContainerFactory")
	public void tombstoneDois(@Payload String message) throws DataCiteApiException {
		var event = mapper.readValue(message, TombstoneEvent.class);
		service.tombstoneRecord(event);
	}

}
