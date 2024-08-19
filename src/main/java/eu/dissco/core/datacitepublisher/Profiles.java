package eu.dissco.core.datacitepublisher;

import lombok.Value;

@Value
public class Profiles {

  private Profiles() {
  }

  public static final String SANDBOX = "sandbox";
  public static final String PRODUCTION = "production";

}
