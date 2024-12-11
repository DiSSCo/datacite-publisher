package eu.dissco.core.datacitepublisher.properties;

import eu.dissco.core.datacitepublisher.domain.datacite.DcPublisher;
import eu.dissco.core.datacitepublisher.domain.datacite.UriScheme;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties("doi")
public class DoiProperties {
  @NotBlank
  private String prefix;
  @NotBlank
  private String publisherName = "Distributed System of Scientific Collections";
  @NotBlank
  private String publisherIdentifier = "https://ror.org/0566bfb96";
  @NotBlank
  private String landingPageSpecimen;
  @NotBlank
  private String landingPageMedia;
  private final DcPublisher defaultPublisher = new DcPublisher(publisherName, publisherIdentifier,
      UriScheme.ROR.getSchemeName(), UriScheme.ROR.getUri());

  public static final String SPECIMEN_ALT_ID_TYPE = "primarySpecimenObjectId";
  public static final String MEDIA_ALT_ID_TYPE = "primaryMediaId";
  public static final String SPECIMEN_TYPE = "Digital Specimen";
  public static final String MEDIA_TYPE = "Media Object";
  public static final String RESOURCE_TYPE_GENERAL = "Dataset";
  public static final String DC_EVENT = "publish";
  public static final String SCHEMA_VERSION = "https://datacite.org/schema/kernel-4.4";


}
