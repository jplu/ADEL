package fr.eurecom.adel.commons.exceptions;

/**
 * @author Julien Plu on 2019-02-18.
 */
public class CoNLLMalformedException extends Exception {
  public CoNLLMalformedException(final String errorMessage) {
    super(errorMessage);
  }
  
  public CoNLLMalformedException(final String errorMessage, final Throwable err) {
    super(errorMessage, err);
  }
}
