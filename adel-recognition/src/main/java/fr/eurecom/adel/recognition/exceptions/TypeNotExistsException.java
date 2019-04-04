package fr.eurecom.adel.recognition.exceptions;

/**
 * @author Julien Plu on 2019-03-13.
 */
public class TypeNotExistsException extends Exception {
  public TypeNotExistsException(final String errorMessage) {
    super(errorMessage);
  }
  
  public TypeNotExistsException(final String errorMessage, final Throwable err) {
    super(errorMessage, err);
  }
}
