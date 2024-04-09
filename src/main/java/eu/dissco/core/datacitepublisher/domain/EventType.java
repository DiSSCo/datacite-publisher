package eu.dissco.core.datacitepublisher.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

public enum EventType {
  @JsonProperty("create") CREATE,
  @JsonPropertyOrder("update") UPDATE;

}
