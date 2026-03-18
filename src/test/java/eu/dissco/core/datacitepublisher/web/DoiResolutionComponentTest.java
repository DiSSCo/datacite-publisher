package eu.dissco.core.datacitepublisher.web;

import static eu.dissco.core.datacitepublisher.TestUtils.DOI;
import static org.mockito.BDDMockito.then;

import eu.dissco.core.datacitepublisher.client.DoiClient;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DoiResolutionComponentTest {

	@Mock
	private DoiClient doiClient;

	private DoiResolutionComponent doiResolutionComponent;

	@BeforeEach
	void setup() {
		doiResolutionComponent = new DoiResolutionComponent(doiClient);
	}

	@Test
	void testResolveHandle() throws Exception {
		// Given

		// When
		doiResolutionComponent.resolveDois(List.of(DOI));

		// Then
		then(doiClient).should().resolveDois(List.of(DOI));
	}

}
