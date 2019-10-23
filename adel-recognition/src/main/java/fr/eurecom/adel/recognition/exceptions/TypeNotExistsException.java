package fr.eurecom.adel.recognition.exceptions;

/**
 * @author Julien Plu on 2019-03-13.
 */
public class TypeNotExistsException extends Exception {
  private static final long serialVersionUID = -2101902359093722753L;
  
  public TypeNotExistsException(final String errorMessage) {
    super(errorMessage);
  }
  
  public TypeNotExistsException(final String errorMessage, final Throwable err) {
    super(errorMessage, err);
  }
}
