package eu.dissco.core.datacitepublisher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaRetryTopic;

@EnableKafkaRetryTopic
@SpringBootApplication
@ConfigurationPropertiesScan
public class DatacitePublisherApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatacitePublisherApplication.class, args);
	}

}
