package eu.dissco.core.datacitepublisher.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum EventType {
  @JsonProperty("create") CREATE,
  @JsonProperty("update") UPDATE,
  @JsonProperty("tombstone") TOMBSTONE;

}
