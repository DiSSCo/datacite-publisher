package eu.dissco.core.datacitepublisher.exceptions;

public class FdoProfileException extends RuntimeException {
  public FdoProfileException(String field, String value, String doi){
    super("Unable to parse " + field + " \"" + value + "\" for doi " + doi);
  }

}
