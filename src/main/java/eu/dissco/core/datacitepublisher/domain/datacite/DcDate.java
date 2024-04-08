package eu.dissco.core.datacitepublisher.domain.datacite;

import com.fasterxml.jackson.annotation.JsonProperty;

record DcDate (
    String date
){
  @JsonProperty("dateType")
  private static final String DATE_TYPE = "Issued";

}
