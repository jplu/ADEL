package fr.eurecom.adel.commons.exceptions;

/**
 * @author Julien Plu on 2019-02-28.
 */
public class NIFMalformedException extends Exception {
  public NIFMalformedException(final String errorMessage) {
    super(errorMessage);
  }
  
  public NIFMalformedException(final String errorMessage, final Throwable err) {
    super(errorMessage, err);
  }
}
