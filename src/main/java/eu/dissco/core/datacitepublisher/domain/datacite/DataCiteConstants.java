package eu.dissco.core.datacitepublisher.domain.datacite;

import java.util.List;

public class DataCiteConstants {
  private DataCiteConstants(){}
  public static final String PREFIX = "10.3535";
  public static final List<DcCreator> DC_CREATORS;

  public static final String ALT_ID_TYPE_DS = "primarySpecimenObjectId";
  public static final String ALT_ID_TYPE_MO = "primaryMediaId";

  static {
     DC_CREATORS = List.of(new DcCreator("Distributed System of Scientific Collections",
        List.of(new DcNameIdentifiers("https://ror.org", "https://ror.org/0566bfb96", "ROR"))));
  }

  public static final String LANDING_PAGE_DS = "https://sandbox.dissco.tech/ds/";
  public static final String LANDING_PAGE_MO = "https://sandbox.dissco.tech/dm/";

  public static final String RESOURCE_TYPE_GENERAL  = "Other";
  public static final String DC_EVENT = "publish";
  public static final String SCHEMA_VERSION = "http://datacite.org/schema/kernel-4.4";
  public static final String PUBLISHER = "Distributed System of Scientific Collections";


}
