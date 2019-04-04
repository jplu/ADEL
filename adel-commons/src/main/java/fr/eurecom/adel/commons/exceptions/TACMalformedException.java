package fr.eurecom.adel.commons.exceptions;

/**
 * @author Julien Plu on 2019-03-02.
 */
public class TACMalformedException extends Exception {
  public TACMalformedException(final String errorMessage) {
    super(errorMessage);
  }
  
  public TACMalformedException(final String errorMessage, final Throwable err) {
    super(errorMessage, err);
  }
}
