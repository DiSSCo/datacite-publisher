package eu.dissco.core.datacitepublisher.domain.datacite;

public class DataCiteConstants {
  private DataCiteConstants(){}
  public static final String PREFIX = "10.3535";
  public static final String ALT_ID_TYPE_DS = "primarySpecimenObjectId";
  public static final String ALT_ID_TYPE_MO = "primaryMediaId";
  public static final String LANDING_PAGE_DS = "https://sandbox.dissco.tech/ds/";
  public static final String LANDING_PAGE_MO = "https://sandbox.dissco.tech/dm/";
  public static final String TYPE_DS = "Digital Specimen";
  public static final String TYPE_MO = "Media Object";
  public static final String RESOURCE_TYPE_GENERAL  = "Dataset";
  public static final String DC_EVENT = "publish";
  public static final String SCHEMA_VERSION = "https://datacite.org/schema/kernel-4.4";
  public static final DcPublisher PUBLISHER;

  static {
    PUBLISHER = new DcPublisher();
  }


}
