package fr.eurecom.adel.recognition.exceptions;

/**
 * @author Julien Plu on 2019-03-13.
 */
public class MappingNotExistsException extends Exception {
  private static final long serialVersionUID = -1638450642016819040L;
  
  public MappingNotExistsException(final String errorMessage) {
    super(errorMessage);
  }
  
  public MappingNotExistsException(final String errorMessage, final Throwable err) {
    super(errorMessage, err);
  }
}
