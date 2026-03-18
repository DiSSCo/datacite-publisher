package eu.dissco.core.datacitepublisher.configuration;

import com.fasterxml.jackson.annotation.JsonSetter.Value;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration {

	public static final String DATE_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

	public static final DateTimeFormatter DATACITE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
		.withZone(ZoneId.of("UTC"));

	@Bean
	public JsonMapper jsonMapper() {
		return JsonMapper.builder()
			.findAndAddModules()
			.defaultDateFormat(new SimpleDateFormat(DATE_STRING))
			.defaultTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC))
			.withConfigOverride(List.class, cfg -> cfg.setNullHandling(Value.forValueNulls(Nulls.AS_EMPTY)))
			.withConfigOverride(Map.class, cfg -> cfg.setNullHandling(Value.forValueNulls(Nulls.AS_EMPTY)))
			.withConfigOverride(Set.class, cfg -> cfg.setNullHandling(Value.forValueNulls(Nulls.AS_EMPTY)))
			.build();
	}

	@Bean
	public XmlMapper xmlMapper() {
		return new XmlMapper();
	}

}
