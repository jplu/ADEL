package fr.eurecom.adel.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fr.eurecom.adel.datatypes.Query;

/**
 * @author Julien Plu
 */
public class ContentValidator implements ConstraintValidator<Content, Query> {
  @Override
  public final void initialize(final Content newA) {
  }
  
  @Override
  public final boolean isValid(final Query newT,
                               final ConstraintValidatorContext newConstraintValidatorContext) {
    boolean res = true;
    
    if (newT.getContent() == null && newT.getUrl() == null) {
      res = false;
    }
  
    if (newT.getContent() != null && newT.getUrl() != null) {
      res = false;
    }
    
    return res;
  }
}
