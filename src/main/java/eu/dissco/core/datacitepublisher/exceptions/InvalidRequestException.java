package eu.dissco.core.datacitepublisher.exceptions;

public class InvalidRequestException extends Exception {
  public InvalidRequestException(){
    super();
  }

  public InvalidRequestException(String message) {
    super(message);
  }

}
