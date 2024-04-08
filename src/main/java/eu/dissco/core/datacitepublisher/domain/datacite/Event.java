package eu.dissco.core.datacitepublisher.domain.datacite;

public enum Event {
  PUBLISH ("publish"),
  REGISTER("register"),
  HIDE("hide");

  final String action;

  private Event(String action){
    this.action = action;
  }

  @Override
  public String toString() {
    return action;
  }

}
